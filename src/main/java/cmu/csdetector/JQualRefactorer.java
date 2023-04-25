package cmu.csdetector;

import cmu.csdetector.console.ToolParameters;
import cmu.csdetector.console.output.ObservableExclusionStrategy;
import cmu.csdetector.heuristics.Cluster;
import cmu.csdetector.heuristics.ClusterManager;
import cmu.csdetector.metrics.MethodMetricValueCollector;
import cmu.csdetector.metrics.MetricName;
import cmu.csdetector.metrics.TypeMetricValueCollector;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.resources.loader.JavaFilesFinder;
import cmu.csdetector.resources.loader.SourceFile;
import cmu.csdetector.resources.loader.SourceFilesLoader;
import cmu.csdetector.smells.ClassLevelSmellDetector;
import cmu.csdetector.smells.MethodLevelSmellDetector;
import cmu.csdetector.smells.Smell;
import cmu.csdetector.smells.SmellName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.cli.ParseException;
import org.eclipse.jdt.core.dom.ASTNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JQualRefactorer {
    private Map<Type, List<Smell>> classSmells = new HashMap<>();
    private Map<Method, List<Smell>> methodSmells = new HashMap<>();

    private static final Integer CC_THRESHOLD = 6;
    public static final Integer EXTRACT = 1;
    public static final Integer MOVE = 2;
    public static final Integer EM = 3;

    public static void main(String[] args) throws IOException{
        JQualRefactorer instance = new JQualRefactorer();

        instance.start(args);

    }

    private void start(String[] args) throws IOException {
        ToolParameters parameters = ToolParameters.getInstance();


        try {
            parameters.parse(args);
        } catch (ParseException exception) {
            System.out.println(exception.getMessage());
            parameters.printHelp();

            System.exit(-1);
        }


        System.out.println(new Date());
        List<String> sourcePaths = List.of(parameters.getValue(ToolParameters.SOURCE_FOLDER));
        List<Type> allTypes = this.loadAllTypes(sourcePaths);

        collectTypeMetrics(allTypes);

        detectSmells(allTypes);

        getRefactoringOperation();

//        saveSmellsFile(allTypes);

        System.out.println(new Date());

    }
    private List<Type> loadAllTypes(List<String> sourcePaths) throws IOException {
        List<Type> allTypes = new ArrayList<>();

        JavaFilesFinder sourceLoader = new JavaFilesFinder(sourcePaths);
        SourceFilesLoader compUnitLoader = new SourceFilesLoader(sourceLoader);
        List<SourceFile> sourceFiles = compUnitLoader.getLoadedSourceFiles();

        for (SourceFile sourceFile : sourceFiles) {
            allTypes.addAll(sourceFile.getTypes());
        }
        return allTypes;
    }

    private void collectTypeMetrics(List<Type> types) {
        for (Type type : types) {
            TypeMetricValueCollector typeCollector = new TypeMetricValueCollector();
            typeCollector.collect(type);

            this.collectMethodMetrics(type);
        }
    }

    private void collectMethodMetrics(Type type) {
        for (Method method: type.getMethods()) {
            MethodMetricValueCollector methodCollector = new MethodMetricValueCollector();
            methodCollector.collect(method);
        }
    }
    private void detectSmells(List<Type> allTypes) {
        List<Type> complexClassList = new ArrayList<>();
        for(Type type : allTypes) {
            // It is important to detect certain smells at method level first, such as Brain Method
            MethodLevelSmellDetector methodLevelSmellDetector = new MethodLevelSmellDetector();

            for(Method method : type.getMethods()) {
                List<Smell> smells = methodLevelSmellDetector.detect(method);
                method.addAllSmells(smells);
                methodSmells.put(method, smells);

            }

            // Some class-level smell detectors rely on method-level smells as part of their detection
            ClassLevelSmellDetector classLevelSmellDetector = new ClassLevelSmellDetector();
            List<Smell> smells = classLevelSmellDetector.detect(type);

            type.addAllSmells(smells);
            classSmells.put(type, smells);
            System.out.println(complexClassList);
        }
    }

    private boolean isComplexClass(List<Smell> smells){
        for(Smell s:smells){
            if(s.getName() == SmellName.ComplexClass) {
                return true;
            }
        }
        return false;
    }
    private boolean isFeatureEnvyPresent(List<Smell> smells){
        for(Smell s:smells){
            if(s.getName() == SmellName.FeatureEnvy) {
                return true;
            }
        }
        return false;
    }

    private  void findExtractMethodOpportunities(){
        for(Type c: classSmells.keySet()){
            if(isComplexClass(classSmells.get(c))){
                for(Method m: c.getMethods()){
                    if(m.getMetricValue(MetricName.CC) > CC_THRESHOLD){

//                        TODO: move the extact opp setup to this class (refer any test)
                        //RefactoringOperation- INTERFACE
                        //ExtractRefactoring - Class extending above
                        RefactoringOperation operation = new ExtractRefactoring();

//                        TODO: implement getRecommendatin mehtod to get top 3 recommmnedations;
                        List<Cluster> suggestions = operation.getRecommendation();

//                        TODO: print user firendly recommendation of cluster
                        printRecommendations(suggestions);

//                        TODO: save text to output.json
                        saveRecommendationToFile("EXTRACT METHOD", c, m, suggestions);
                    }
                }
            }
        }
    }

    private  void findMovetMethodOpportunities(){
                for(Method m: methodSmells.keySet()){
                    if(isFeatureEnvyPresent(methodSmells.get(m)){

//                        TODO: move the extact opp setup to this class (refer any test)
                        //RefactoringOperation- INTERFACE
                        //ExtractRefactoring - Class extending above
                        RefactoringOperation operation = new MoveRefactoring();

//                        TODO: implement getRecommendatin mehtod to get top target class;
                        Type taregtClass = operation.getRecommendation();

//                        TODO: print user firendly recommendation of cluster
                        printRecommendations(taregtClass);

//                        TODO: save text to output.json
                        saveRecommendationToFile("MOVE METHOD", c, m, suggestions);
                    }
                }
            }
        }
    }
//    private void saveSmellsFile(List<Type> smellyTypes) throws IOException {
//        ToolParameters parameters = ToolParameters.getInstance();
//        File smellsFile = new File(parameters.getValue(ToolParameters.SMELLS_FILE));
//        BufferedWriter writer = new BufferedWriter(new FileWriter(smellsFile));
//        System.out.println("Saving smells file...");
//
//        GsonBuilder builder = new GsonBuilder();
//        builder.addSerializationExclusionStrategy(new ObservableExclusionStrategy());
//        builder.disableHtmlEscaping();
//        builder.setPrettyPrinting();
//        builder.serializeNulls();
//
//        Gson gson = builder.create();
//        gson.toJson(smellyTypes, writer);
//        writer.close();
//    }

    private void getRefactoringOperation(){
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("What refactoring operation do you want to run on selected project ?");
        System.out.println("Select 1 for Extract Method");
        System.out.println("Select 2 for Move Method");
        System.out.println("Select 3 for Extract followed by Move Method");
        int n = reader.nextInt();
        if(n==EXTRACT){
            System.out.println("Finding Extract opportunitites ....");
            findExtractMethodOpportunities();
        } else if(n==MOVE){
            System.out.println("Finding Move opportunitites ....");
            findExtractMethodOpportunities();
        } else if(n==EM){
            System.out.println("Finding Extract and Move opportunitites ....");
            findExtractMoveMethodOpportunities();
        } else {
            System.out.println("Invalid entry. try again !");
            getRefactoringOperation();
        }




    };

}
