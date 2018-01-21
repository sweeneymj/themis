#!/usr/bin/perl
#
# perl script for generating IP databases for use in Themis
#
# (c) sweeneymj@gmail.com
#

use strict;
use Getopt::Std;
use MaxMind::DB::Writer::Tree;

# first step - get and check input options
our($opt_D, $opt_d, $opt_h, $opt_i, $opt_o, $opt_t);
getopts('Dd:hi:o:t:');

if (defined $opt_h) {
	&usage;
}
 
if (!defined $opt_d) {
	&usage("no description set");
}
 
if (!defined $opt_i) {
	&usage("no input file set");
}
 
if (!defined $opt_o) {
	&usage("no output file set");
}
 
if (!defined $opt_t) {
	&usage("no name set");
}
 
# setup the mindmax database
my %types = (
	note => 'utf8_string',
);
 
my $tree = MaxMind::DB::Writer::Tree->new(
	ip_version            => 4,
	record_size           => 32,
	database_type         => $opt_t,
	languages             => ['en'],
	description           => { en => $opt_d },
	map_key_type_callback => sub { $types{ $_[0] } },
);
 
# open source
# - we expect the source file to contain IP or IP range, a comma, an optional note
# - lines starting with '#' are ignored 
# - empty lines are ignored

open(F, $opt_i);
while(<F>){
	if ($_ =~ /^#/) {
		next;
	}
	if ($_ =~ /^\s*$/) {
		next;
	}
	if ($_ =~ /^([\d\.\/]+),(.+)/) {
		my ($network, $note) = ($1, $2);
		($network !~ /\/\d+/) && ($network .= '/32');
		($opt_D) && (print "$network, $note\n");
		$tree->insert_network(
			$network,
			{
				note => $note
			},
		);
	}
}
close(F);

# write out to a file
open my $fh, '>:raw', $opt_o;
$tree->write_tree($fh);

# usage sub
sub usage {
	my $message = shift;

	if (defined $message) {
		print "\nerror - $message\n";
	}

	print<<EOL;

usage: $0 OPTION

	-D			debug mode
	-d <description>	database description
	-h			help
	-i <filename>		source data containing IP ranges and values
	-o <filename>		output database
	-t <database name>	database name

EOL
	exit(0);
	}
