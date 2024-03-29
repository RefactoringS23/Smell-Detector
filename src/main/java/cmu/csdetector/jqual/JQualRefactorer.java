package cmu.csdetector.jqual;

import cmu.csdetector.console.ToolParameters;
import cmu.csdetector.console.output.ObservableExclusionStrategy;
import cmu.csdetector.heuristics.Cluster;
import cmu.csdetector.jqual.recommendation.ExtractMethodRecommendation;
import cmu.csdetector.jqual.recommendation.Recommendation;
import cmu.csdetector.jqual.refactoringOperations.ExtractMethodRefactoring;
import cmu.csdetector.jqual.refactoringOperations.MoveMethodRefactoring;
import cmu.csdetector.jqual.refactoringOperations.RefactoringOperation;
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
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JQualRefactorer {
    private Map<Type, List<Smell>> classSmells = new HashMap<>();
    private Map<Method, List<Smell>> methodSmells = new HashMap<>();

    private static final int CC_THRESHOLD = 6;
    public static final int EXTRACT = 1;
    public static final int MOVE = 2;
    public static final int EM = 3;

    private int freshPrintCounter = 0;

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

        getRefactoringOperation(allTypes);

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
    private boolean isBrainMethod(List<Smell> smells){
        for(Smell s:smells){
            if(s.getName() == SmellName.BrainMethod) {
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

    private  void findExtractMethodOpportunities(List<Type> allTypes) throws IOException {
        for(Type c: classSmells.keySet()){
            if(isComplexClass(classSmells.get(c))){
                for(Method m: c.getMethods()){
                    if(isBrainMethod(methodSmells.get(m))){
                        ExtractMethodRefactoring operation = new ExtractMethodRefactoring(c, m);
                        List<Cluster> suggestions =  operation.getTopRecommendations();
                        for(Cluster s: suggestions){
                            Recommendation r = new ExtractMethodRecommendation(c,m,s);
                            printRecommendations(r);
                            saveRecommendationsToFile(r);
                        }
                    }
                }
            }
        }
    }

    private  void findMoveMethodOpportunities(List<Type> allTypes) throws IOException {
        for(Method m: methodSmells.keySet()){
            if(isFeatureEnvyPresent(methodSmells.get(m))){

                MethodDeclaration declaration = (MethodDeclaration) m.getNode();
                Type parent = null;
                parent = getType(allTypes, m, parent);
                RefactoringOperation operation = new MoveMethodRefactoring(parent, m, allTypes);
                Recommendation r = operation.getRecommendation();
                printRecommendations(r);
                saveRecommendationsToFile(r);
                System.out.println("end of move");
            }
        }
    }

    private static Type getType(List<Type> allTypes, Method m, Type parent) {
        for(Type t: allTypes){
            if(t.getMethods().contains(m)){
                parent = t;
                break;
            }
        }
        return parent;
    }

    private  void findExtractAndMoveMethodOpportunities(List<Type> allTypes) throws IOException {
        for(Type c: classSmells.keySet()){
            if(isComplexClass(classSmells.get(c))){
                for(Method m: c.getMethods()){
                    if(m.getMetricValue(MetricName.CC) > CC_THRESHOLD){

                        ExtractMethodRefactoring operation1 = new ExtractMethodRefactoring(c, m);
                        Recommendation extractedMethod = operation1.getRecommendation();
                        printRecommendations(extractedMethod);
                        saveRecommendationsToFile(extractedMethod);

                        ExtractMethodRecommendation em = (ExtractMethodRecommendation) operation1.getRecommendation();
                        Cluster extractedCluster = operation1.getBestCluster();
                        RefactoringOperation operation2 = new MoveMethodRefactoring(extractedCluster.getParentClass(),extractedCluster, allTypes);

                        Recommendation r = operation2.getRecommendation();
                        printRecommendations(r);
                        saveRecommendationsToFile(r);
                    }
                }
            }
        }
    }

    private void saveSmellsFile(List<Type> smellyTypes) throws IOException {
        ToolParameters parameters = ToolParameters.getInstance();
        File smellsFile = new File(parameters.getValue(ToolParameters.SMELLS_FILE));
        BufferedWriter writer = new BufferedWriter(new FileWriter(smellsFile));
        System.out.println("Saving smells file...");

        GsonBuilder builder = new GsonBuilder();
        builder.addSerializationExclusionStrategy(new ObservableExclusionStrategy());
        builder.disableHtmlEscaping();
        builder.setPrettyPrinting();
        builder.serializeNulls();

        Gson gson = builder.create();
        gson.toJson(smellyTypes, writer);
        writer.close();
    }

    private void printRecommendations(Recommendation r) {
        System.out.println(r.getReadableString());
    }

    private void saveRecommendationsToFile(Recommendation r) throws IOException {

        try {
            ToolParameters parameters = ToolParameters.getInstance();
            File suggestionsFile = new File(parameters.getValue(ToolParameters.SUGGESTION_FILE));
    
            if (suggestionsFile.exists() && freshPrintCounter == 0) {
                suggestionsFile.delete();
            }
            freshPrintCounter++;
    
            BufferedWriter writer = new BufferedWriter(new FileWriter(suggestionsFile, true));
    
            writer.write(r.getReadableString());
            writer.newLine();
    
            System.out.println("Saving recommendations file...");
    
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRefactoringOperation(List<Type> allTypes) throws IOException {
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.println("What refactoring operation do you want to run on selected project ?");
        System.out.println("Select 1 for Extract Method");
        System.out.println("Select 2 for Move Method");
        System.out.println("Select 3 for Extract followed by Move Method");
        int n = reader.nextInt();
        switch(n) {
            case EXTRACT:
                System.out.println("Finding Extract opportunitites ....");
                findExtractMethodOpportunities(allTypes);
                break;
            case MOVE:
                System.out.println("Finding Move opportunitites ....");
                findMoveMethodOpportunities(allTypes);
                break;
            case EM:
                System.out.println("Finding Extract and Move opportunitites ....");
                findExtractAndMoveMethodOpportunities(allTypes);
                break;
            default:
                System.out.println("Invalid entry. try again !");
                getRefactoringOperation(allTypes);
        }
    }

}
