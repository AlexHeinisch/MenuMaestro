import xml.etree.ElementTree as ET
import sys

# Path to the JaCoCo XML report file
jacoco_report_file = 'target/site/jacoco-aggregate/jacoco.xml'

# Get the required coverage from the command-line argument
try:
    required_coverage = float(sys.argv[1])  # Pass as first argument
except IndexError:
    print("Usage: python check_coverage.py <required_coverage>")
    sys.exit(1)
except ValueError:
    print("Invalid value for required coverage, please provide a numeric value.")
    sys.exit(1)


try:
    # Parse the XML file
    tree = ET.parse(jacoco_report_file)
    root = tree.getroot()

    # Find the branch coverage data
    counter = root.findall(".//counter[@type='BRANCH']")
    if not counter:
        print("Could not find branch coverage data in the JaCoCo report.")
        exit(1)

    # Pick the last occurrence
    last_counter = counter[-1]

    # Extract the 'missed' and 'covered' values from the XML
    missed = int(last_counter.get('missed', 0))
    covered = int(last_counter.get('covered', 0))
    print(missed)
    print(covered)

    # Calculate the total lines and the line coverage percentage
    total_lines = missed + covered
    if total_lines == 0:
        print("No lines were executed or missed, cannot calculate coverage.")
        exit(1)

    branch_coverage = (covered / total_lines) * 100

    # Output the calculated coverage
    print(f"Branch Coverage: {branch_coverage:.2f}%")

    # Check if the coverage meets the required threshold (e.g., 80%)
    # required_coverage = 80.0  # Set your desired threshold here
    if branch_coverage < required_coverage:
        print(f"Coverage is below the required threshold of {required_coverage}%.")
        exit(1)
    else:
        print("Coverage meets the required threshold.")
        exit(0)

except FileNotFoundError:
    print(f"JaCoCo aggregate report not found at {jacoco_report_file}.")
    exit(1)

except ET.ParseError as e:
    print(f"Error parsing the JaCoCo XML report: {e}")
    exit(1)