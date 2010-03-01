#This script appends missing translations to Venice locale files so that
#non developer translators can easily fix missing translations.
#Once translations are added, they can be moved to the proper location in 
#the file  

use strict;

my $locale;
my %engIndex = ();
my @locales = ("en", "zh", "sv", "pl", "it", "fr", "ca");
my %localeFileRefs = ();
my %localeRefs = ();

my $basePath = "src/nz/org/venice/util/Locale/";
my $localePathKey = "venice_LANG.properties";

sub mychomp
{
    my $ref = $_[0];
    my $sep = $_[1];

    {
	local $/ = $sep;
	chomp $$ref;	
    }

}

sub buildFile
{
    my $locale = $_[0];
    my $path;
    my $line;
    my @lines = ();
    my @entries = ();
    my $debug = 0;

    $path = $basePath . $localePathKey;
    $path =~ s/LANG/$locale/;

    open (LOC , "<$path") or die "Couldnt open $path for reading";
    
    my $index = 0;
    while ($line = <LOC>) {
	chomp $line;
	$lines[$index] = $line;
	$index++;
    }
    close LOC;
    
    for ($index = 0; $index <= $#lines; $index++) {
	$line = $lines[$index];
	
	if ($line =~ m/\#/) {
	    next;
	}

	my $j = 1;
	my $entry = $line;
	my $nextLine = $lines[$index + $j];
	
	while ($nextLine !~ m/=/ &&
	    $nextLine !~ m/\#/ &&
	    $nextLine ne "") {
	   
	    $entry .= $nextLine;
	    $j++;
	    $nextLine = $lines[$index + $j];	    	    
	}
	#We joined $j-1 lines together
	#So we don't want to add them again
	if ($j >= 2) {
	    $index += $j - 1 
	}
	push @entries, $entry;	
    }
    $localeFileRefs{$locale} = \@entries;
}

sub buildIndex
{
    my $locale = $_[0];
    my $line;
    my @tokens;
    my %newLocale = ();
    my $path;
    my @entries;
    my $fileRef = $localeFileRefs{$locale};

    #$path = $basePath . $localePathKey;
    #$path =~ s/LANG/$locale/;

    print "building Index for $locale\n";
    
    #open (LOC, "<$path") or die "Couldnt open $path for reading\n";
    
    foreach $line (@$fileRef) {
	
	if ($line =~ m/\#/) {
	    next;
	}
	if ($line eq "") {
	    next;
	}
	
	@tokens = split "=", $line;
	&mychomp(\$tokens[0], " ");
       
	if ($tokens[0] eq "" ) {
	    next;
	}
	$newLocale{$tokens[0]} = $tokens[1];
	#print "|$tokens[0]|, |$tokens[1]|, $newLocale{$tokens[0]}\n";
    }

    $localeRefs{$locale} = \%newLocale;
   
    close LOC;    
}

sub fixLocale
{
    my $locale = $_[0];
    my $engIndexRef = $localeRefs{$locales[0]};
    my $localeIndexRef = $localeRefs{$locale};
    my @missingKeys = ();
    my @missingValues = ();
    my $bannerLarge = "######################################################";
    my $banner = "#";

    my $ref = ref $engIndexRef;

    my $path;
    $path = $basePath . $localePathKey;
    $path =~ s/LANG/$locale/;

    #Don't fix the English Locale - it's the reference
    if ($locale eq "en") {
	return;
    }

    print "Adding annotations for $locale\n";


    open (LOC, ">>$path") or die "Couldn't open $path for append\n";
    
    foreach my $englishKey (keys %$engIndexRef) {
	my $englishValue = $engIndexRef->{$englishKey};

	if (!exists $localeIndexRef->{$englishKey}) {
	    push @missingKeys, $englishKey;
	} else {
	    my $localeValue = $localeIndexRef->{$englishKey};
	    
	    if ($localeValue eq "") {
		push @missingValues, $englishKey;
	    }	    
	}
    }    
    
    print LOC "$bannerLarge\n";
    print LOC "$banner Missing Translation Entries Section $banner\n";
    print LOC "$banner These are entries which exist in the English Localfile $banner\n";
    print LOC "$banner But not in this locale $banner\n";  
    print LOC "$banner Replace the FIXME values with an appropriate translation $banner\n";
    print LOC "$banner Generated by fix-locales.pl $banner#\n";
    print LOC "$bannerLarge\n";
    foreach my $missingKey (@missingKeys) {
	print LOC "$missingKey = FIXME\n";
    }
    print LOC "$banner END Missing Translaction Entries Section $banner\n";
    
    print LOC "$bannerLarge\n";
    print LOC "$banner Missing Value Section $banner\n";
    print LOC "$banner These are entries which are missing values \n";
    
    foreach my $missingKey (@missingValues) {
	print LOC "$missingKey = FIXME\n";
    }

    print LOC "$banner End Missing Values Section $banner\n";
    print LOC "$bannerLarge\n";
    close LOC;
    
}

sub main
{

    foreach $locale (@locales) {
	&buildFile($locale);

	# For each locale, build a map of keys to values
	#For each english key, check there is a corresponding key in the
	#other locales
	#If the key exists but has no value, append to end of file with note 
	#if the key is missing, append to end of file with note
	&buildIndex($locale);

	&fixLocale($locale);
    }

}

&main;



