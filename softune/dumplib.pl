#!/usr/bin/perl -w
# dumplib <libfile> [-x]
#
# Dump Softune V6 library file
#
# REQUIREMENTS:
#
# Author: coderat
#
# History:
# 01Feb14 coderat: creation
#

my $checksum = 0;
my $stringArray;

my @modulesArray;

my %sectionTypes = (
	0x20 => STACK,
	0x40 => IO,
	0x10 => DATA,
	0x30 => CONST,
	0x00 => CODE
	);

my %sectionFlags = (
	0x40 => R,
	0x10 => W,
	0x04 => X,
	0x01 => I
	);

my $currentSectionNum = -1;
#-------------------------------------------------------------
sub readData($) {
	my $len = $_[0];
	my $buf;

  my $n = read (IN, $buf, $len);

  if (!defined $n) {
    die "!!! Problem reading: $!\n";
  }

  if ($n != $len) {
    die "!!! Not enough data in read\n";
  }

	# 8-bit checksum
  $checksum += unpack("%8C*", $buf);
  return $buf;
}

#-------------------------------------------------------------
sub formatDate($) {

	my @date = unpack('a4 a2 a2 a2 a2 a2',$_[0]);
	return sprintf('%s-%s-%s %s:%s:%s', @date);
}
#-------------------------------------------------------------
sub extract($ $) {
	my $len = $_[1];

	open(OUT, '> '.$_[0].'.obj')
		or die "can't create object file\n";
	binmode OUT;

	my $buf;
	my $n = read (IN, $buf, $len);

  if (!defined $n) {
    die "!!! Problem reading: $!\n";
  }

  if ($n != $len) {
    die "!!! Not enough data in read\n";
  }

  print OUT $buf;

	close OUT;
}

#-------------------------------------------------------------
my $doExtract = 0;

if (@ARGV<1) {
	print "Softune V6 library dump utility\n\nUse: [perl] dumplib.pl <libfile> [-x]\n";
	print "  -x    extract modules\n";
	exit 0;
}

if (@ARGV>1 && $ARGV[1] eq '-x') {
	$doExtract = 1;
}

open(IN,"$ARGV[0]")
	or die "can't open library file\n";
binmode IN;

$fileLength = -s IN;

my $offset = 0;

my $currentModule=0;

# records aligned to 16 bytes
for (my $recordSize=0; $offset < $fileLength; $offset += (($recordSize+15) & (~15))) {

	seek(IN, $offset, 0)
		or die "Problem seeking: $!";

	$checksum = 0;

	my $recordID = ord(readData(1));

  if ($recordID==0xE0 || $recordID==0x81) {
		$recordSize = unpack('n',readData(2));
  } else {
		$recordSize = unpack('N',readData(4));
  }

	my $record = readData( $recordSize - (tell(IN)-$offset) );
	my $recordChecksum = ord(chop($record));

	die sprintf ("!!! Wrong record checksum %02X, expected 0xFF\n", $checksum)
		if (($checksum&0xFF) != 0xFF);

  printf "Record 0x%02X, size=%d\n", $recordID, $recordSize;

	if ($recordID==0xE0) {        #--------------------------------------
		print "  Start\n";

		my ($void, $dateCreated,$dateRevised,$modules, $symbols, $offsetSymbols, $type, $CPU)=unpack("C a14 a14 N N N N a*",$record);

		die "!!! unknown format\n"
			if ($void != 1  || $type!=7);
		print "  CPU=$CPU modules=$modules symbols=$symbols created=".formatDate($dateCreated)." revised=".formatDate($dateRevised)."\n";

	} elsif ($recordID==0xE2) {   #--------------------------------------

		print "  Modules:\n";

		while (length($record)!=0) {
			my ($date, $n, $size, $void1, $exports, $lenName, $name);

			($date, $n, $void1, $size, $exports, $lenName, $record) = unpack ("a14 N a N N N a*", $record);
			($name,$record) = unpack ("a$lenName a*", $record);

			my @module = ($name, $size);
			push(@modulesArray,\@module);

			print "  name=\"$name\" size=$size entry date=".formatDate($date).' '.unpack("H*",$void1)." exports=$exports\n";
		}

	} elsif ($recordID==0xE4) {   #--------------------------------------

		print "  Symbols:\n";
		while (length($record)!=0) {

			my ($moduleNum, $len, $name);
			($moduleNum, $len, $record) = unpack ("N N a*", $record);
			($name,$record) = unpack ("a$len a*", $record);

			my $module = $modulesArray[$moduleNum];

			print "  $name from module=\"".$$module[0]."\"\n";

		}

	} elsif ($recordID==0xEF) {   #--------------------------------------
		print "  End\n";

	} elsif ($recordID==0x81) {   #--------------------------- Skip module

		if (!defined($modulesArray[$currentModule])) {
			die "!!! unknown module\n";
		}
		my $module = $modulesArray[$currentModule++];

		print '  Module:'.$$module[0]."\n";

		if ($doExtract) {

			seek(IN, $offset, 0)
				or die "Problem seeking: $!";

			extract($$module[0], $$module[1]);
		}

		# skip rest of this module
		$recordSize = $$module[1];

	} else {
		die "!!! unknown record type\n"
	}

}


close IN;

exit 0;

