package com.mhall119;
import org.apache.commons.cli.*;

public class NexmoApp {

    
    public static void main( String[] args ) {
        Options options = new Options();
        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
        System.out.println("Hello Nexmo");
    }
}
