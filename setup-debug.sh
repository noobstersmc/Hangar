#!/bin/bash
set -e
# Colors
yellow="\e[0;33m"
green="\e[0;92m"
white="\e[0;97m"
bold="\e[1m"
uline="\e[4m"
reset="\e[0m"
# Variables
steps=0
launch_file=0
outputfile="purpur-1.16.5.jar"
downloadUrl="https://api.pl3x.net/v2/purpur/1.16.5/1171/download"
startScript="start-debug.sh"
# Id debug folder doesn't exist, create it.
if ! [ -d debug/ ]; then
    mkdir -p debug
    steps=$((steps + 1))
fi
# If server jar doesn't exist, download it.
if ! [ -f debug/$outputfile ]; then
    echo -e -n $green
    echo -e "Downloading Purpur server..."
    echo -e -n $reset
    curl -o debug/$outputfile $downloadUrl
    steps=$((steps + 1))
fi
# Create the start script if it does not exist.
if ! [ -f debug/$startScript ]; then
    echo -e -n $green
    echo "Creating start script..."
    echo -e -n $reset
    # Cat the start debug script
    cat >>debug/$startScript <<EOF
#!/bin/bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar outputfile
EOF
    steps=$((steps + 1))
fi
# Check if the current directory contains a compile version of the plugin in the /build/libs/ folder. Match regex *-all.jar
if [ -f build/libs/*-all.jar ]; then
    echo -e -n $green
    echo "Moving binary to debug/plugins/"
    echo -e -n $reset
    mkdir -p debug/plugins/
    cp build/libs/*-all.jar debug/plugins/
    steps=$((steps + 1))
fi
# If server.properties does not exist, write one with the following defaults:
if ! [ -f debug/server.properties ]; then
    echo -e -n $green
    echo "Creating server.properties..."
    echo -e -n $reset
    echo "allow-nether=false" >debug/server.properties
    echo "" >>debug/server.properties
    steps=$((steps + 1))
fi
# If bukkit.yml does not exist, write one with the following defaults:
if ! [ -f debug/bukkit.yml ]; then
    echo -e -n $green
    echo "Creating bukkit.yml..."
    echo -e -n $reset
    cat >debug/bukkit.yml <<EOF
settings:
    allow-end: false
EOF
    steps=$((steps + 1))
fi
# Make the start script executable
chmod +x debug/$outputfile
# Accept EULA
echo "eula=true" >debug/eula.txt
# Tell the user to create a launch.json for the debug profile
if ! [ -f .vscode/launch.json ]; then
    # Set launch file to 1
    launch_file=$((1))
    # Print the template onto the console.
    echo -e -n $yellow
    echo -e "\nTemplate (launch.json):$reset$bold \n{
  \"version\": \"0.2.0\",
  \"configurations\": [
    {
      \"type\": \"java\", // Required
      \"name\": \"Connect Debug Server\", // Whatever name
      \"request\": \"attach\", // Required
      \"mainClass\": \"us.jcedeno.hangar.paper.Hangar\", // Your plugins's main class
      \"hostName\": \"localhost\", // Debug server host
      \"port\": 5005 // Debug server port (different than minecraft server port)
    }
  ]
}\n"
fi
if [ $launch_file -eq 1 ]; then
    echo -e -n $green
    echo -e "Please create a$white launch.json$green file in your VS Code folder $white(.vscode/launch.json)$green using the configuration above as a reference"
    echo -e -n $reset
fi
# Print final output depending on steps achived.
if ! [ $steps -eq 0 ]; then
    echo -e -n $green
    echo -e "Setup finished. $steps step(s) was/were required.\nOnce you've setup your$white launch.json$green, cd into the debug folder and run$white sh start-debug.sh$green to start the debug server. "
    echo -e -n $reset
else
    echo -e -n $green
    echo "Setup finished. No steps were required."
    echo -e -n $reset
fi
# Remind the user that for debug mode to work, a JVM with DCEVM is required,
echo -e -n $green
echo -e "Please remember that your JVM must support$white DCEVM$green for debug mode to work.$white TravaOpenJDK$green supports it natively but there isn't a JDK 16 release yet."


echo -e -n $reset
