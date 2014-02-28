#!/usr/bin/perl -w
# dumpncp <file.ncp>
#
# Dump .ncp file
#
# REQUIREMENTS:
#
# Author: coderat
#
# History:
# 28Feb14 coderat: creation
#

my $checksum = 0;

my %baseProfiles = (
	0x00C3 => 'VIVID',
	0x0001 => 'STANDARD',
	0x03C2 => 'NEUTRAL',
	0x0014 => 'D2XMODE1',
	0x03D5 => 'D2XMODE2',
	0x00D6 => 'D2XMODE3',
	0x0486 => 'PORTRAIT',
	0x04C7 => 'LANDSCAPE',
	0x064D => 'MONOCHROME'
	);

my %monoFilter = (
  'N/A' => 'N/A',
	0 => 'OFF',
	1 => 'YELLOW',
	2 => 'ORANGE',
	3 => 'RED',
	4 => 'GREEN'
	);

my %monoToning = (
  'N/A' => 'N/A',
	0 => 'B/W',
	1 => 'SEPIA',
	2 => 'CYANOTYPE',
	3 => 'RED',
	4 => 'YELLOW',
	5 => 'GREEN',
	6 => 'BLUE GREEN',
	7 => 'BLUE',
	8 => 'PURPLE BLUE',
	9 => 'RED PURPLE'
	);

my $VALUE_AUTO = -128;
my $VALUE_CURVE = -127;
my $VALUE_NA = 127;

#-------------------------------------------------------------
sub byte2int($@) {
	my $value = $_[0]-0x80;

	if ($value == 127) {
		return 'N/A';
	}

	for ( shift @_ ; 0<@_; shift @_) {

		if ($_[0]==$value) {

			if ($value==$VALUE_AUTO) {
				return 'auto';

			} elsif ($value==$VALUE_CURVE) {
				return '(curve)';

			} else {
				return '???';
			}
		}
	}
	return $value;
}
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
#  $checksum += unpack("%8C*", $buf);
  return $buf;
}

#-------------------------------------------------------------
if (@ARGV<1) {
	print "NCP dump utility\n\nUse: [perl] dumpncp.pl <file.ncp>\n";
	exit 0;
}

open(IN,"$ARGV[0]")
	or die "can't open .ncp file\n";
binmode IN;

#$fileLength = -s IN;

my $offset = 0;

if (readData(4) ne "NCP\x00") {
	die "!!! Wrong signature";
}


for (;;) {

	my $recordID = unpack ('N', readData(4));

	if ($recordID==0) {
		last;
	}
	my $recordSize = unpack ('N', readData(4));
  printf "Record 0x%02X, size=%d\n", $recordID, $recordSize;

  my $record = readData($recordSize);

  if ($recordID==1) {
  	my ($version, $name, $basedProfile);
  	($version, $name, $basedProfile, $modified, $record) = unpack ('a4 Z20 n C x a*', $record);

		print "Version=$version name=\'$name\' based on ".$baseProfiles{$basedProfile}."\n";

		if ($modified==0) {
			print "Settings unmodified:\n";
		} elsif ($modified==1) {
			print "Settings modified (but are same as defaults):\n";
		} else {
			print "Settings modified:\n";
		}

		my $i;

		($i, $record) = unpack('C a*', $record);
		print '  Sharpening:'.byte2int($i, $VALUE_AUTO)."\n";

		($i, $record) = unpack('C a*', $record);
		print '  Contrast:'.byte2int($i, $VALUE_AUTO,$VALUE_CURVE)."\n";

		($i, $record) = unpack('C a*', $record);
		print '  Brightness:'.byte2int($i, $VALUE_CURVE)."\n";

		($i, $record) = unpack('C a*', $record);
		print '  Saturation:'.byte2int($i, $VALUE_AUTO)."\n";

		($i, $record) = unpack('C a*', $record);
		print '  Hue:'.byte2int($i)."\n";

		($i, $record) = unpack('C a*', $record);
		print '  Filter:'.$monoFilter{byte2int($i)}."\n";

		($i, $record) = unpack('C a*', $record);
		print '  Toning:'.$monoToning{byte2int($i)};

		($i, $record) = unpack('C a*', $record);
		print ' strength:'.byte2int($i)."\n";

	} elsif ($recordID==2) {	# curve

		my ($a,$b);

		($a,$b, $record)=unpack ('x2 C2 a*',$record);
		print "input black point:$a\n";
		print "input white point:$b\n";

		($a,$b)=unpack ('x2 C2',$record);
		print 'input halftone point:'.(0.01*$b+$a)."\n";

		($a,$b, $record)=unpack ('C2 x2 a*',$record);
		print "out min:$a\n";
		print "out max:$b\n";

		my @points;
		my $numPoints;
		($numPoints, @points) = unpack ('C C57', $record);

		print "Polynom points:\n";
		for (my $i=0; $i< $numPoints*2; $i+=2) {
			print '  in:'.$points[$i].' out:'.$points[$i+1]."\n";
		}
		$record = unpack ('x58 a*', $record);

		print "Points table:\n";
		print "  Input Out:\n";
		for (my $i=0; $i<256; $i++) {

			my $out;
			($out,$record) = unpack ('n a*', $record);

			printf ("   %3d %5d\n", $i, $out);
		}
	}
}

exit 0;
__END__

