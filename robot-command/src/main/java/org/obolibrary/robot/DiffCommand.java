package org.obolibrary.robot;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command to get the differences between two ontology files.
 *
 * @author <a href="mailto:james@overton.ca">James A. Overton</a>
 */
public class DiffCommand implements Command {
  /** Logger. */
  private static final Logger logger = LoggerFactory.getLogger(DiffCommand.class);

  /** Store the command-line options for the command. */
  private Options options;

  /** Initialze the command. */
  public DiffCommand() {
    Options o = CommandLineHelper.getCommonOptions();
    o.addOption("l", "left", true, "load left ontology from file");
    // o.addOption("L", "left-iri",  true, "load left ontology from IRI");
    o.addOption("r", "right", true, "load right ontology from file");
    // o.addOption("R", "right-iri", true, "load right ontology from IRI");
    o.addOption("o", "output", true, "save results to file");
    options = o;
  }

  /**
   * Name of the command.
   *
   * @return name
   */
  public String getName() {
    return "diff";
  }

  /**
   * Brief description of the command.
   *
   * @return description
   */
  public String getDescription() {
    return "find the differences between two ontologies";
  }

  /**
   * Command-line usage for the command.
   *
   * @return usage
   */
  public String getUsage() {
    return "robot diff --left <file> " + "--right <file> " + "--output <file>";
  }

  /**
   * Command-line options for the command.
   *
   * @return options
   */
  public Options getOptions() {
    return options;
  }

  /**
   * Handle the command-line and file operations for the DiffOperation.
   *
   * @param args strings to use as arguments
   */
  public void main(String[] args) {
    try {
      execute(null, args);
    } catch (Exception e) {
      CommandLineHelper.handleException(getUsage(), getOptions(), e);
    }
  }

  /**
   * Given an input state and command line arguments, report on the differences between ontologies,
   * if any, and return the state unchanged.
   *
   * @param state the state from the previous command, or null
   * @param args the command-line arguments
   * @return the input state, unchanged
   * @throws Exception on any problem
   */
  public CommandState execute(CommandState state, String[] args) throws Exception {
    CommandLine line = CommandLineHelper.getCommandLine(getUsage(), getOptions(), args);
    if (line == null) {
      return null;
    }

    IOHelper ioHelper = CommandLineHelper.getIOHelper(line);
    if (state == null) {
      state = new CommandState();
    }

    OWLOntology leftOntology = null;
    if (state != null && state.getOntology() != null) {
      leftOntology = state.getOntology();
    }
    if (leftOntology == null) {
      String leftOntologyPath = CommandLineHelper.getOptionalValue(line, "left");
      if (leftOntologyPath != null) {
        leftOntology = ioHelper.loadOntology(leftOntologyPath);
      }
    }
    if (leftOntology == null) {
      throw new IllegalArgumentException("left ontology must be specified");
    }

    OWLOntology rightOntology = null;
    if (rightOntology == null) {
      String rightOntologyPath = CommandLineHelper.getOptionalValue(line, "right");
      String leftOntologyPath = CommandLineHelper.getOptionalValue(line, "left");
      if (rightOntologyPath != null) {
        rightOntology = ioHelper.loadOntology(rightOntologyPath);
      }
    }
    if (rightOntology == null) {
      throw new IllegalArgumentException("right ontology must be specified");
    }

    Writer writer;
    String outputPath = CommandLineHelper.getOptionalValue(line, "output");
    if (outputPath != null) {
      writer = new FileWriter(outputPath);
    } else {
      writer = new PrintWriter(System.out);
    }

    DiffOperation.compare(leftOntology, rightOntology, writer);
    writer.flush();
    writer.close();

    return state;
  }
}
