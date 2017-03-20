# openfoodfacts

A personal project to look up a product's EAN code based on its name. Remote database used is Open Food Facts: https://fr.openfoodfacts.org/

# Input & Output

Input should be a txt file with one product name/description per line

Output should be a JSON containing key-value pairs per product with the original product name, the EAN code and the description. 
In case the EAN is not found, add the original product description in the output file.

# several instructions

1. a flat jar (runnable.jar) and output (out.txt) is already contained if you do not want to build and run.

2. for building and creating your own jar :

   $cd imstest
   
   $mvn clean install
   
   $mvn package
   
   a jar named imstest-1.0-SNAPSHOT.jar will be created at target/
   
   use it as : $ java -jar imstest-1.0-SNAPSHOT.jar -i <inputfilepath> -o <outputfilepath>
   
   an output file will be created if it does not exist.

3. unit test is not perfect (honestly speaking it is only a sample), since I know it is a bad idea to transform json to String. 

4. It is possible to change the database by changing the variable "searchURL" in the code
