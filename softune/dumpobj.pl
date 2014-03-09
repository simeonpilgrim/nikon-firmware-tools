#!/usr/bin/perl -w
# dumpobj <objfile>
#
# Dump Softune V6 object file
#
# REQUIREMENTS:
#
# Author: coderat
#
# History:
# 01Feb14 coderat: creation
# 07Feb14 coderat: added relocation description
#

my $checksum = 0;
my $stringArray;

my @sectionsArray;

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

my %symbolTypes = (
	0x0100 => 'public',
	0x0200 => 'local',
	0x0300 => 'type'
	);

my $currentSectionNum = -1;

my @importsArray;

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
sub getString($) {
	return unpack("x$_[0] Z*", $stringArray);
}

#-------------------------------------------------------------
sub formatDate($) {

	my @date = unpack('a4 a2 a2 a2 a2 a2',$_[0]);
	return sprintf('%s-%s-%s %s:%s:%s', @date);
}
#-------------------------------------------------------------
if (@ARGV<1) {
	print "Softune V6 object dump utility\n\nUse: [perl] dumpobj.pl <objfile>\n";
	exit 0;
}

open(IN,"$ARGV[0]")
	or die "can't open object file\n";
binmode IN;

$fileLength = -s IN;

my $offset = 0;

for (my $recordSize=0; $offset < $fileLength; $offset+=$recordSize) {

	seek(IN, $offset, 0)
		or die "Problem seeking: $!";

	$checksum = 0;

	my $recordID = ord(readData(1));

  if ($recordID==0x81) {
		$recordSize = unpack('n',readData(2));
  } else {
		$recordSize = unpack('N',readData(4));
  }

	my $record = readData( $recordSize - (tell(IN)-$offset) );
	my $recordChecksum = ord(chop($record));

	die sprintf ("!!! Wrong record checksum %02X, expected 0xFF\n", $checksum)
		if (($checksum&0xFF) != 0xFF);

  printf "Record 0x%02X, size=%d\n", $recordID, $recordSize;

	if ($recordID==0x81) {        #--------------------------------------
		print "  Start\n";

	} elsif ($recordID==0x82) {   #--------------------------------------
		# strings
		$stringArray = $record;
		print "  Strings\n";

	} elsif ($recordID==0x84) {   #--------------------------------------

		# compiler record ?
		my ($void, $date);
		($void, $date, $record) = unpack ("a2 a14 a*",$record);

		die '!!! unknown format:'.unpack('H*',$void)."\n"
			if ($void ne "\x02\xFF");

		my $n = length($record) - 8;
		my ($str1, $str2) = unpack ("x$n N N",$record);
		print "  Module=\"".getString($str1)."\" instruction set=".getString($str2);
		print ' compiled='.formatDate($date)."\n";

	} elsif ($recordID==0x85) {   #--------------------------------------

		print "  Need libraries:\n";

		my $count;
		($count, $record) = unpack ('N a*',$record);

		for (; $count>0 ; $count--) {

			my $str;
			($str, $record) = unpack ("N a*", $record);

			print '  '.getString($str)."\n";
		}

	} elsif ($recordID==0x90) {   #--------------------------------------

		# source file
		my ($date,$str)=unpack("x21 a14 x11 N",$record);
		print "  source=\"".getString($str)."\" compiled=".formatDate($date)."\n";

	} elsif ($recordID==0x92) {   #--------------------------------------

		# section definition

		my ($locType, $addr, $void, $size, $align, $linkType, $flags, $str) = unpack("C N C N N C C N",$record);

		die "!!! unknown format:$void"
			if ($void != 0);

		my @section = (getString($str), $sectionTypes{$linkType&0xF0});
		push (@sectionsArray, \@section);

		print "  Section: name=\"".$section[0]."\"";
		print ' type='.$section[1];
		if ($locType==0) {
			printf " address=0x%08X",$addr;
		}
		print " size=$size align=$align flags=";

		foreach (keys %sectionFlags) {

			if ($flags & $_) {
				print $sectionFlags{$_};
				$flags &= (~$_);

			} else {
				print '-';
			}
		}
		die "!!! unknown flag $flags\n"
			if ($flags!=0);

		print ' link method=';
		$linkType &= 0xF;
		if ($linkType==0xF) {

			print 'nolink';

		} elsif ($linkType==0) {

			print 'concat';

		} else {
			die "!!! unknown link method $linkType\n"
		}
		print ($locType==1 ? " location=relative\n" : " location=absolute\n");


	} elsif ($recordID==0x94) {   #--------------------------------------

		print "  Imports:\n";

		while (length($record)!=0) {

			my ($void, $str);
			($void, $str, $record) = unpack ("C N a*", $record);

			die '!!! unknown format'.unpack('H*',$void)."\n"
				if ($void != 0xFF);

			push (@importsArray, getString($str));

			print '  '.getString($str)."\n";
		}

	} elsif ($recordID==0x96) {   #--------------------------------------

		print "  Exports:\n";
		while (length($record)!=0) {

			my ($void, $addr, $str, $sectionNum);
			($void, $sectionNum, $addr, $str, $record) = unpack ("C N N N a*", $record);

			die "!!! unknown format:$void\n"
				if ($void != 2);

			my $section = $sectionsArray[$sectionNum];

			print '  '.getString($str)." section=\"".$$section[0]."\"";
			printf " address=0x%08X\n", $addr;

		}

	} elsif ($recordID==0x97) {   #-------------------------------------

		print "  Symbols:\n";
		my $count;
		($count, $record) = unpack ('N a*',$record);

		for (; $count>0 ; $count--) {

			my ($type, $str);
			($str, $type, $record) = unpack ("N n a*", $record);

			print '  '.getString($str);

			if (defined($symbolTypes{$type})) {
				print ' ('.$symbolTypes{$type}.")\n";
			} else {
				die '!!! unknown symbol type:'.$type;
			}
		}

	} elsif ($recordID==0xA0) {   #--------------------------------------
		# data for section

		my $void;
		($void, $currentSectionNum) = unpack ('N N',$record);

		my $section = $sectionsArray[$currentSectionNum];
		print "  Load section=\"".$$section[0]."\"\n";

		die '!!! unknown format:'.$void
			if ($void!=0);

	} elsif ($recordID==0xA2) {   #--------------------------------------

		my ($addr,$size) = unpack ('N N',$record);

		printf "  Section data: addr=0x%08X size=%d file offset=%d\n", $addr, $size, $offset + 5 +8;

	} elsif ($recordID==0xA4) {   #--------------------------------------

		print "  Relocations:\n";

		while (length($record)!=0) {

			my ($type, $addr, $void, $len, $calcString);
			($type, $addr, $startBit, $numBits, $void, $len, $record) = unpack ("C N C C N N a*", $record);

			printf "  type=$type addr=0x%08X\n", $addr;
			print "    replace bits in MSB bitstring starting from bit $startBit with $numBits lower bits of result\n";

			($calcString, $record) = unpack ("a$len a*", $record);
			print "    Calculation (in ungarn notation):\n";

			while (length($calcString)!=0) {
				my ($op, $n, $str);

				($op,$calcString) = unpack ('C a*', $calcString);

				if ($op==0xFF) {
					last;
				} elsif ($op==0) {

					($n,$calcString) = unpack ('N a*', $calcString);
					my $section = $sectionsArray[$n];

					print '    ADDRESSOF module section '."\"".$$section[0]."\"\n";

				} elsif ($op==2) {

					($n,$calcString) = unpack ('N a*', $calcString);


					print "    ADDRESSOF ".$importsArray[$n]."\n";

				} elsif ($op==3) {

					($n,$calcString) = unpack ('n a*', $calcString);

					($str,$calcString) = unpack ("a$n a*", $calcString);
					$str = "\x00" x (4-$n).$str;

					printf "    CONSTANT 0x%x\n", unpack ('N', $str);

				} elsif ($op==4) {

					($n,$calcString) = unpack ('N a*', $calcString);
					my $section = $sectionsArray[$n];

					print '    SIZEOF section '."\"".$$section[0]."\"\n";

				} elsif ($op==5) {

					($n,$calcString) = unpack ('N a*', $calcString);
					my $section = $sectionsArray[$n];

					print '    ADDRESSOF section '."\"".$$section[0]."\"\n";

				} elsif ($op==0x20) {
					print "    ADD\n";
				} elsif ($op==0x21) {
					print "    SUB\n";
				} elsif ($op==0x29) {
					print "    SHR\n";
				} elsif ($op==0x2C) {
					print "    AND\n";
				} else {
					die '!!! Unknown operation code';
				}
			}
		}

	} elsif ($recordID==0xB0 || $recordID==0xB2 || $recordID==0xB3 ||
	         $recordID==0xB4 || $recordID==0xD8 || $recordID==0xC2 ||
	         $recordID==0xC3 || $recordID==0xC6 || $recordID==0xC8 ||
	         $recordID==0xD0 || $recordID==0xD2 || $recordID==0xD4) {
		# debug ?

	} elsif ($recordID==0xFF) {   #--------------------------------------
		print "  End\n";

	} else {
		die '!!! unknown record type:$recordID'
	}

}


close IN;

exit 0;

