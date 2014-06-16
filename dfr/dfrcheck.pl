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
# 10May14 coderat: add -n option to find new addresses
# 19May14 coderat: performance optimisation
# 20May14 coderat: function parameters check
#

my @iranges;
my @cranges;
my @dranges;

my %Symbols;
my %reversedSymbols;

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

sub parsePrototype($) {
		my $str = $_[0];

		$str =~ s#^\s*([0-9A-Za-z_:.@\$\?]+)##;
		my $name = $1;

		# [spaces](...)[spaces][\r]
		$str =~ s#^\s*##;
		if (length($str)>0) {

			if (ord($str)==ord('(')) {
				#start of arguments

				if ($str =~ m#\((.*)\)\s*$#) {
					$str = $1;

					# remove comments
					$str =~ s#/\*.*\*/##gm;

					# [spaces]
					$str =~ s#^\s*##;

					while (length($str)>0) {

						# reg '['...']'[,]
						# reg : Rn | $an | $vn | $sn

						if ($str =~ s#^\$*[A-Za-z]\d+\s*\[##) {

							# OUT name
							# IN name[; OUT name]
							while (1) {

								if (!($str =~ s#^\s*(in|out)\s+##i)) {
									print "!!! Wrong or missing parameter type in $name\n";
									return $name;
								}

								# name[spaces]
								if (!($str =~ s#^[0-9A-Za-z_:.@\$\?]+\s*##)) {
									print "!!! Missing parameter name in $name\n";
									return $name;
								}

								if (ord($str)==ord(';')) {
									$str = substr ($str,1);
									next;

								} elsif (ord($str)==ord(']')) {
									$str =~ s#^]\s*##;
									last;
								}
							}

						} else {
							print "!!! Incorrect parameter in $name\n";
							last;
						}

						if (length($str)==0) {
							last;
						}

						if (!($str =~ s#^,\s*##)) {
							print "!!! Missing coma in parameters of $name\n";
							last;
						}
					}
				} else {
					print "!!! Missing ) in $name\n";
				}

			} else {
				print "!!! Missing ( in $name\n";
			}
		}
		return $name;
}

# ------------------------------------------------------------------------
sub testConsistency {

	open(IN,"$ARGV[0]")
		or die "can't open dfr file\n";

	while (<IN>) {
		my $buf = $_;
		# remove comments
		$buf =~ s#\x23.*##gm;

		# find all input ranges
		while ($buf =~ m#\-i[ ]+0x([0-9a-fA-F]+)-0x([0-9a-fA-F]+)=0x([0-9a-fA-F]+)#gm) {

			my @range = (hex $1, hex $2);

			testRange(@iranges, @range, 'input');

			push(@iranges,\@range);
		}

		# find all ranges
		while ($buf =~ m#\-m[ ]+(0x[0-9a-fA-F]+)-(0x[0-9a-fA-F]+)=([a-zA-Z]{4})#gm) {

			my $start = hex $1;
			my $end = hex $2;
			my $type = $3;

			my @range = (hex $1, hex $2, $3);
			testRange(@cranges, @range, $type);

			# add
			if (lc($type) eq 'code') {
				push(@cranges,\@range);
			} else {
				push(@dranges,\@range);
			}

		}

		# find all symbols
		while ($buf =~ m#\-s[ ]+0x([0-9a-fA-F]+)=([^\r\n]+)#gm) {
			my ($addr,$prototype) = (hex($1),$2);

			if (exists($Symbols{$addr}) ) {
				print "!!! Symbol address $prototype conflicts with ".$Symbols{$addr}."\n";
			}

			my $name = parsePrototype($prototype);

			if (exists($reversedSymbols{$name}) ) {
				print "!!! Symbol name duplicated $name\n";
			}
			$Symbols{$addr} = $prototype;
			$reversedSymbols{$name} = $addr;
		}
	}
	close IN;

	if (@iranges==0) {
		die "!!! No input ranges found\n";
	}

	if (scalar(keys %Symbols)==0) {
		print "!!! No symbols found\n";
	}

	foreach (@cranges) {

		my $crange = $_;

		# test range to be complete in one input range
		for (@iranges) {
			my $range = $_;
			if ($$range[0]<=$$crange[0] && $$range[1]>=$$crange[0] &&
				$$range[0]<=$$crange[1] && $$range[1]>=$$crange[1]) {

					last;
			}
		}
		printf("!!! Not found input range for:%08X-%08X\n",$$crange[0],$$crange[1])
			if (scalar(@cranges)==0);
	}
}
# ------------------------------------------------------------------------
sub dumpSymbols {
	foreach my $addr (sort {$a <=> $b} keys %Symbols) {
		printf ("-s 0x%08X=%s\n", $addr, $Symbols{$addr});
	}
}
# ------------------------------------------------------------------------
sub findNews {

	foreach my $line ( <STDIN> ) {
		if ($line =~ m#\-s[ ]+0x([0-9a-fA-F]+)=#gm) {
			my $addr = hex($1);

			if (!exists($Symbols{$addr}) ) {
				print $line;
			}
		}
	}
}
# ------------------------------------------------------------------------

my $action=0;

if (@ARGV<1) {
	print "DFR utility\n\n";
	print "Use: [perl] dfrcheck.pl <dfr/dtxfile> [-s|-n]\n";
	exit 0;
} elsif (@ARGV>1) {
	if ($ARGV[1] eq '-s') {
		$action=1;
	} elsif ($ARGV[1] eq '-n') {
		$action=2;
	} else {
		die "Unknown option";
	}
}

testConsistency;

if ($action==1) {
	dumpSymbols;
} elsif ($action==2) {
	findNews;
}
exit 0;
