#!/usr/bin/perl -w
# dfrcheck.pl <dfr/dtxfile>
#
# dfr/dtx consistency test utility
#
#
# REQUIREMENTS:
#
# Author: coderat
#
# History:
# 23Jan14 coderat: creation
# 15Feb14 coderat: add -s option for printing all symbols sorted
#

my @cranges;
my @dranges;

my %Symbols;

# ------------------------------------------------------------------------

sub testRange (\@ \@ $) {

	my $ranges = $_[0];
	my $range = $_[1];
	my $name = $_[2];

	for ( @$ranges ) {
		my $old_range = $_;

		if ( $$range[1] <= $$range[0] || !($$old_range[0]>$$range[1] || $$old_range[1]<$$range[0]) ) {
			printf ("!!! %s range %08X-%08X conflicts with range %08X-%08X\n", $name, $$range[0], $$range[1], $$old_range[0], $$old_range[1]);
		}
	}
}

# ------------------------------------------------------------------------
sub testConsistency {

	open(IN,"$ARGV[0]")
		or die "can't open dfr file\n";
	binmode IN;

	$len = -s IN;

	$n = read (IN, $buf,$len);
	if (!defined $n || $n!=$len) {
	  die "Problem reading: $!\n";
	}
	close IN;

	# remove comments
	$buf =~ s#\x23[^\r\n]*##gm;

	my @iranges;

	# find all input ranges
	while ($buf =~ m#\-i[ ]+0x([0-9a-fA-F]+)-0x([0-9a-fA-F]+)=0x([0-9a-fA-F]+)#gm) {

		my @range = (hex $1, hex $2);

		testRange(@iranges, @range, 'input');

		push(@iranges,\@range);
	}

	if (@iranges==0) {
		die "!!! No input ranges found\n";
	}
	# find all ranges
	while ($buf =~ m#\-m[ ]+(0x[0-9a-fA-F]+)-(0x[0-9a-fA-F]+)=([a-zA-Z]{4})#gm) {

		my $start = hex $1;
		my $end = hex $2;
		my $type = $3;

		# test range to be complete in one input range
		for (@iranges) {
			my $range = $_;
			if ($$range[0]<=$start && $$range[1]>=$start &&
				$$range[0]<=$end && $$range[1]>=$end) {

					my @range = ($start, $end, $type);

					testRange(@cranges, @range, $type);

					# add only CODE
					if (lc($type) eq 'code') {
						push(@cranges,\@range);
					}
					last;
			}
		}
		@cranges>0 ||
			die "!!! Not found input range for CODE\n";
	}

	# find all symbols
	while ($buf =~ m#\-s[ ]+0x([0-9a-fA-F]+)=([^\r\n]+)#gm) {
		my ($addr,$name) = (hex($1),$2);

		if (exists($Symbols{$addr}) ) {
			print "!!! Symbol address $name conflicts with ".$Symbols{$addr}."\n";
		}

		my $strippedName = $name;
		$strippedName =~ s#\(.+##;

		foreach (values %Symbols) {

			my $func = $_;
			$func =~ s#\(.+##;

			if ($func eq $strippedName) {
				print "!!! Symbol name duplicated $func\n";
			}
		}
		$Symbols{$addr} = $name;
	}

	if (scalar(keys %Symbols)==0) {
		print "!!! No symbols found\n";
	}
}
# ------------------------------------------------------------------------
sub dumpSymbols {
	foreach my $addr (sort {$a <=> $b} keys %Symbols) {
		printf ("-s 0x%08X=%s\n", $addr, $Symbols{$addr});
	}
}
# ------------------------------------------------------------------------

my $doDump=0;

if (@ARGV<1) {
	print "DFR utility\n\n";
	print "Use: [perl] dfrcheck.pl <dfr/dtxfile> [-s]\n";
	exit 0;
} elsif (@ARGV>1) {
	if ($ARGV[1] eq '-s') {
		$doDump=1;
	} else {
		die "Unknown option";
	}
}

testConsistency;

if ($doDump) {
	dumpSymbols;
}
exit 0;
