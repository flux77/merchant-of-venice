Merchant of Venice, 0.4 alpha 14/Aug/2004
=========================================

Copyright (C) 2003-4, Andrew Leppard (aleppard@picknowl.com.au)
See COPYING.txt for license terms.

1 Introduction

Thank you for trying Merchant of Venice (Venice).

Venice is a stock market trading programme that supports portfolio
management, charting, technical analysis, paper trading and
experimental methods like genetic programming. Venice runs in a
graphical user interface with online help and has full documentation.
Venice runs on UNIX, including Mac OS X and Windows.

2 Running

To run Venice you will need Java J2SE JRE 1.4 or higher, available from the
following location:

        http://java.sun.com/j2se/1.4.2/download.html

The following packages are optional:

   MySQL
        http://www.mysql.com
   MySQL Java Driver
        http://sourceforge.net/projects/mmmysql
     OR http://www.mysql.com/downloads/api-jdbc-stable.html

   PostgreSQL
        http://www.postgresql.org/
   PostgreSQL Java Driver
        http://jdbc.postgresql.org/

   Venice allows you to access stock quotes through a MySQL or PostgrSQL
   database or directly from text files. Reading from text files is easy to
   set up, but the database is much faster and more flexible.

Once they are set up, you can run Venice by typing from the command prompt:

   sh venice

Or in Windows by clicking on the "venice.jar" file.

3 Bug Reports and Enhancements

Please report any bugs that you encounter via the Sourceforge bug
tracking system at:

   https://sourceforge.net/tracker/?func=add&group_id=53631&atid=471025

If you have any ideas for enhancements, please document them via the
Sourceforge enhancement tracking system at:

   https://sourceforge.net/tracker/?func=add&group_id=53631&atid=471028

Or if you have any comments, please don't hesitate to email me,
Andrew Leppard at aleppard@picknowl.com.au.

4 Building

To build Venice you will need the programmes listed in the "Running"
section and the following:

    Ant 1.5 or higher
        http://jakarta.apache.org/builds/jakarta-ant/release/v1.5/

To run the unit tests you will need:

    JUnit 3.8.1 or higher
        http://www.junit.org/

To build Venice type the following:

ant build

You can then run Venice by either:

ant run

Or by creating a jar (ant jar) and then running Venice as described above.

The build file (build.xml) provides other functions for developers (some
of these will only work from source checked out from CVS):

api     Generate a javadoc API of the code
backup  Pulls a backup copy of the CVS tree from Sourceforge and stores it
        in the backup directory.
clean   Removes all built and temporary files
doc     Builds the documentation
jar     Create a java archive file for Venice
release Packages Venice into a file ready for release
test    Runs the automated test suite
web     Packages the web files ready for deployment

5. Thanks

Thanks to the following people for providing additional code, patches or
bugfixes:

Daniel Makovec, Quentin Bossard, Peter Fradley, Bryan Lin, Alberto Nacher &
Matthias Stöckel.

And the following for providing translations:

Quentin Bossard (French), Bryan Lin (Chinese), Alberto Nacher (Italian) &
Pontus Strömdahl (Swedish)
