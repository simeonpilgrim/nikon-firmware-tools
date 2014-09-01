#!/usr/bin/perl -w
# dumpnef <neffile>
#
# Dump NEF file
#
# REQUIREMENTS:
#
# Author: coderat
# License: BSD 3-Clause (http://opensource.org/licenses/BSD-3-Clause)
#
# History:
# 21Jul14 coderat: creation
# 01Aug14 coderat: print real file offsets for embedded TIFF;
#                  embedded LONG values may still differ
#

my $bigEndian = 0;

my $LONG_TAG_TYPE = 0x04;

my %tagType = (
	0x01 => 'BYTE',
	0x02 => 'ASCII',
	0x03 => 'SHORT',
	0x04 => 'LONG',
	0x05 => 'RATIONAL',
	0x07 => 'UNDEFINED',
	0x08 => 'SSHORT',
	0x0A => 'SRATIONAL'
	);

my $SUB_IFD_TAG  = 0x014A;
my $EXIF_IFD_TAG = 0x8769;
my $GPS_IFD_TAG  = 0x8825;

my @tiffIfdTags = ($SUB_IFD_TAG, $EXIF_IFD_TAG, $GPS_IFD_TAG);

my %tiffTagName = (
	0x00FE => 'NewSubfileType',
	0x0100 => 'ImageWidth',
	0x0101 => 'ImageLength',
	0x0102 => 'BitsPerSample',
	0x0103 => 'Compression',
	0x0106 => 'PhotometricInterpretation',
	0x010F => 'Make',
	0x0110 => 'Model',
	0x0111 => 'StripOffsets',
	0x0112 => 'Orientation',
	0x0115 => 'SamplesPerPixel',
	0x0116 => 'RowsPerStrip',
	0x0117 => 'StripByteCounts',
	0x011A => 'XResolution',
	0x011B => 'YResolution',
	0x011C => 'PlanarConfiguration',
	0x0128 => 'ResolutionUnit',
	0x0131 => 'FirmwareVersion',
	0x0132 => 'DateTime',
	0x013B => 'Artist',
	0x014A => 'SubIFD Offsets',
	0x0201 => 'JPEGInterchangeFormat Offset',
	0x0202 => 'JPEGInterchangeFormatLength',
	0x0213 => 'YCbCrPositioning',
	0x0214 => 'ReferenceBlackWhite',
	0x8298 => 'Copyright',
	0x8769 => 'ExifIFD Offset',
	0x8825 => 'GpsIFD Offset',
	0x828D => 'CFAPatternDimensions',
	0x828E => 'CFAPattern',
	0x9003 => 'DateTimeOriginal',
	0x9216 => 'TIFF/EPVersion',
	0x9217 => 'PhotoSensorType',
	);

my $MAKERNOTE_TAG = 0x927C;
my %exifTagName = (
	0x829A => 'ExposureTime',
	0x829D => 'FNumber',
	0x8822 => 'ExposureProgram',
	0x8827 => 'ISO',
	0x8830 => 'SensitivityType',
	0x9003 => 'DateTimeOriginal',
	0x9004 => 'DateTimeDigitized',
	0x9204 => 'ExposureBias',
	0x9205 => 'LensMaxFNumber',
	0x9207 => 'MeteringMode',
	0x9208 => 'LightSourceType',
	0x9209 => 'Flash',
	0x920A => 'FocalLength',
	0x927C => 'MakerNote',
	0x9286 => 'UserComment',
	0x9290 => 'SubSecTime',
	0x9291 => 'SubSecTimeOriginal',
	0x9292 => 'SubSecTimeDigitized',
	0xA217 => 'PhotoSensorType',
	0xA300 => 'FileSource',
	0xA301 => 'SceneType',
	0xA302 => 'CFAPattern',
	0xA401 => 'CustomRendered',
	0xA402 => 'ExposureMode',
	0xA403 => 'WhiteBalance',
	0xA404 => 'DigitalZoomRatio',
	0xA405 => 'FocalLengthIn35mm',
	0xA406 => 'SceneCaptureType',
	0xA407 => 'Gain',
	0xA408 => 'Contrast',
	0xA409 => 'Saturation',
	0xA40A => 'Sharpness',
	0xA40C => 'SubjectDistanceRange'
	);

my @exifIfdTags = ($MAKERNOTE_TAG);

my %gpsTagName = (
	0x0000 => 'Version',
	);

my @nikonIfdTags = (0x0011);

my %nikonTagName = (
	0x0001 => 'Version',
	0x0002 => 'ISO',
	0x0004 => 'FileType',
	0x0005 => 'WhiteBalance',
	0x0007 => 'FocusMode',
	0x0011 => 'PreviewIFD Offset',
	0x001B => 'ImageWidthHeight',
	0x001D => 'SerialNumber',
	0x0023 => 'PictureControlInfo',
	0x0084 => 'LensFocalLengthMaxFNumber',
	0x00A7 => 'ShutterCount'
	);

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
my $headerOffset = 0;

sub setHeaderOffset($) {
	my $ret = $headerOffset;
	$headerOffset = $_[0];
	return $ret;
}
#-------------------------------------------------------------
sub seekFromHeader($) {
	seek(IN, $headerOffset + $_[0], 0)
			or die "Problem seeking: $!";
}
#-------------------------------------------------------------
sub tellFilePos($) {
	return $headerOffset+$_[0];
}
#-------------------------------------------------------------
sub getValueLength ($$) {
	if ($_[0]==1 || $_[0]==2 || $_[0]==7) {
		return $_[1];
	} elsif ($_[0]==3 || $_[0]==8) {
		return $_[1]*2;
	} elsif ($_[0]==4) {
		return $_[1]*4;
	} elsif ($_[0]==5 || $_[0]==10) {
		return $_[1]*8;
	}
	die "!!! unsupported type $_[0]";
}

#-------------------------------------------------------------
sub printValue ($$$) {
	my $value = $_[2];
	my $num;

	# BYTE, small UNDEFINED
	if ($_[0]==1 || ($_[0]==7 && $_[1]<16)) {
		for (my $i=$_[1]; $i>0; $i--) {
			($num,$value) = unpack( 'Ca*', $value);
			printf(" %02X", $num);
		}

	# ASCII
	} elsif ($_[0]==2) {
		print "\"".unpack('A*',$value)."\"";

	# SHORT, SSHORT
	} elsif ($_[0]==3 || $_[0]==8) {

		for (my $i=$_[1]; $i>0; $i--) {
			($num,$value) = unpack( ($bigEndian ? 'na*':'ia*'), $value);
			print " $num";
		}

	# LONG
	} elsif ($_[0]==4) {

		for (my $i=$_[1]; $i>0; $i--) {
			($num,$value) = unpack( ($bigEndian ? 'Na*':'Ia*'), $value);
			print " $num";
		}

	# RATIONAL, SRATIONAL
	} elsif ($_[0]==5 || $_[0]==10) {
		my $denom;

		for (my $i=$_[1]; $i>0; $i--) {
			($num, $denom, $value) = unpack( ($bigEndian ? 'NNa*':'IIa*'), $value);
			print " $num/$denom";
		}

	# long UNDEFINED
	} elsif ($_[0]==7) {
		print "(?)";
	} else {
		die "!!! unsupported type $_[0]";
	}
}
#-------------------------------------------------------------
sub printIFD($\%;\@);

sub printIFD($\%;\@) {
	my $offset = $_[0];
	my $tagName = $_[1];
	my $ifdTags = $_[2];
	my @IFDs;

	print "Offset: ".tellFilePos($offset)."\n";
	seekFromHeader($offset);
	my $buf = readData(2);
	my $num = unpack( ($bigEndian ? 'n':'i'), $buf);
	print "Entrys: $num\n";
	$buf = readData(12*$num);

	for (my $i=0; $i <$num; $i++) {
		my ($tag,$type,$count, $value);

		# directory entry
		($tag, $type, $count, $buf) = unpack( ($bigEndian ? 'nnNa*':'iiIa*'), $buf);

		my $len = getValueLength($type,$count);

		# value
		if ($len>4) {
			($offset, $buf) = unpack( ($bigEndian ? 'Na*':'Ia*'), $buf);

			seekFromHeader($offset);
			$value = readData($len);
		} else {
			# embedded
			($value, $buf) = unpack( 'a'.$len.($len!=4 ? 'x'.(4-$len) : '').'a*', $buf);
		}

		printf('Tag:%04X type: %s[%d] ',$tag,$tagType{$type},$count);
		if ($len>4) {
			printf( '(offset %d) ', tellFilePos($offset));
		}
		if (exists($$tagName{$tag})) {
			print $$tagName{$tag};
		}
		print(':');
		printValue($type, $count, $value);
		print "\n";

		if (defined($ifdTags)) {
			for (@$ifdTags) {
				if ($tag==$_) {
					if ($type == $LONG_TAG_TYPE) {
						for (; $count>0; $count--) {
							($offset,$value) = unpack( ($bigEndian ? 'Na*':'Ia*'), $value);
							push(@IFDs, [$tag,$offset]);
						}
					} else {
						push(@IFDs, [$tag,$offset]);
					}
					last;
				}
			}
		}
	}
	return $num,\@IFDs;
}
#-------------------------------------------------------------
sub printTiff {
	# header
	my $str = unpack('a2', readData(2));

	if ($str eq 'MM') {
		$bigEndian = 1;
	} elsif ($str eq 'II') {
		$bigEndian = 0;
	} else {
		die "!!! unknown endianess:$str";
	}
	print "Endianess: ".($bigEndian ?'big':'little')."\n";

	my ($num, $offset) = unpack( ($bigEndian ? 'n N':'x2 i I'), readData(6));
	print "ID: $num\n";
	($num==0x2A or
		die "!!! unsupported ID");

	# IFD
	while ($offset!=0) {
		print "IFD ";

		my ($entrys,$ifds) = printIFD($offset, %tiffTagName, @tiffIfdTags);
		for (@$ifds) {
			my $entry = $_;

			if ($$entry[0] == $SUB_IFD_TAG) {
				print "SubIFD ";
				printIFD($$entry[1], %tiffTagName, @tiffIfdTags);

			} elsif ($$entry[0] == $EXIF_IFD_TAG) {
				print "ExifIFD ";
				my ($i,$exifIfds) = printIFD($$entry[1], %exifTagName, @exifIfdTags);

				for (@$exifIfds) {
					my $entry = $_;

					if ($$entry[0] == $MAKERNOTE_TAG) {

						seekFromHeader($$entry[1]);

						# Nikon MakerNote embedd anoter TIFF
						if (readData(6) eq "Nikon\x00") {
							printf("Nikon MakerNote version:%02X.%02X\n",unpack('CC',readData(2)));

							readData(2);
							# TIFF header
							if (readData(4) eq "\x4D\x4D\x00\x2A") {
								my $oldHeaderOffset = setHeaderOffset($$entry[1]  + 6 + 2 + 2);
								print "NikonIFD ";
								my ($j,$nikonSubIfds) = printIFD(unpack('N', readData(4)), %nikonTagName, @nikonIfdTags);

								for (@$nikonSubIfds) {
									my $entry = $_;
									print "Nikon PreviewIFD ";
									printIFD($$entry[1], %tiffTagName);
								}
								setHeaderOffset($oldHeaderOffset);
							}

						} else {
							print "!!! Unknown MakerNote tag\n";
						}
					}
				}

			} elsif ($$entry[0] == $GPS_IFD_TAG) {
				print "GpsIFD ";
				printIFD($$entry[1], %gpsTagName);
			}
		}

		seekFromHeader($offset+$entrys*12+2);
		# next IFD offset
		$offset = unpack( ($bigEndian ? 'N':'I'), readData(4));
	}
}
#-------------------------------------------------------------
if (@ARGV<1) {
	print "NEF dump utility\n\nUse: [perl] dumpnef.pl <neffile>\n";
	exit 0;
}

open(IN,"$ARGV[0]")
	or die "can't open nef file\n";
binmode IN;

printTiff;

close IN;

exit 0;
__END__

