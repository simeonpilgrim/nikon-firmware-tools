#!/usr/bin/perl -w
# dumpncp <nkldfile>
#
# Dump NKLD file
#
# REQUIREMENTS:
#
# Author: coderat
# License: BSD 3-Clause (http://opensource.org/licenses/BSD-3-Clause)
#
# History:
# 29Apr14 coderat: creation
#

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

  return $buf;
}

#-------------------------------------------------------------
if (@ARGV<1) {
	print "NKLD dump utility\n\nUse: [perl] dumpnkld.pl <sourcefile>\n";
	exit 0;
}

open(IN,"$ARGV[0]")
	or die "can't open source file\n";
binmode IN;

# header
my $tmp = readData(16);

my ($dataOffset, $totalLength, $majorVersion, $minorVersion, $entryCount, $magic, $dataLength, $unknown) = unpack('n n C C n N n n', $tmp);

($magic == 0x87C7CAAC) ||
	die "!!! unsupported magic $magic\n";
($majorVersion == 1) ||
	die "!!! unsupported magic $majorVersion\n";

print "Version:$majorVersion.$minorVersion\n";
print "Total length:$totalLength\n";
printf("Unknown:0x%04X\n",$unknown);
print "Data length: $dataLength at $dataOffset\n";
print "Entrys: $entryCount\n";

seek(IN, $dataOffset, 0)
		or die "Problem seeking: $!";

if ( ($dataOffset + $dataLength) > $totalLength || $totalLength > -s IN) {
	die "!!! bad NKLD header\n";
}

for (my $i=0; $i < $entryCount; $i++) {
	# entry header
	my $tmp = readData(16);

	my ($entryLength, $id, $subId, $prop) = unpack ('n C C x2 C', $tmp);
	printf("Lens ID: %02X subID:%02X prop:%02X ", $id, $subId, $prop);
	print "entry length: $entryLength\n";

	seek(IN, $entryLength - 16, 1)
		or die "Problem seeking: $!";
}

printf("Crc:0x%04X\n", unpack ('n', readData(2)));

close IN;

exit 0;
__END__

