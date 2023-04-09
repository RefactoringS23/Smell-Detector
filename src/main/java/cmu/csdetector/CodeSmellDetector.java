package cmu.csdetector;

import cmu.csdetector.console.ToolParameters;
import cmu.csdetector.console.output.ObservableExclusionStrategy;
import cmu.csdetector.heuristics.Cluster;
import cmu.csdetector.heuristics.ClusterLine;
import cmu.csdetector.metrics.MethodMetricValueCollector;
import cmu.csdetector.metrics.TypeMetricValueCollector;
import cmu.csdetector.smells.ClassLevelSmellDetector;
import cmu.csdetector.smells.MethodLevelSmellDetector;
import cmu.csdetector.smells.Smell;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cmu.csdetector.resources.Method;
import cmu.csdetector.resources.Type;
import cmu.csdetector.resources.loader.JavaFilesFinder;
import cmu.csdetector.resources.loader.SourceFile;
import cmu.csdetector.resources.loader.SourceFilesLoader;
import org.apache.commons.cli.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CodeSmellDetector {

    public static void main(String[] args) throws IOException{
        CodeSmellDetector instance = new CodeSmellDetector();

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

        saveSmellsFile(allTypes);

        System.out.println(new Date());

        SortedMap<Integer, HashSet<String>> table = createDummyHashMapForClustering();

        Set<Cluster> clusters = Cluster.makeClusters(table);

        Set<Cluster> allClusters = Cluster.createMergedClusters(clusters);
        
        System.out.println(allClusters);

    }

    private SortedMap<Integer, HashSet<String>> createDummyHashMapForClustering() {
        SortedMap<Integer, HashSet<String>> table = new TreeMap<>();
 
        
        HashSet<String> set2 = new HashSet<>();
        set2.add("manifests");
        set2.add("rcs.length");
        set2.add("rcs");
        set2.add("length");
        table.put(2, set2);
        
        HashSet<String> set3 = new HashSet<>();
        set3.add("i");
        set3.add("rcs.length");
        set3.add("rcs");
        set3.add("length");
        table.put(3, set3);
        
        HashSet<String> set4 = new HashSet<>();
        set4.add("rec");
        table.put(4, set4);
        
        HashSet<String> set5 = new HashSet<>();
        set5.add("rcs");
        set5.add("i");
        table.put(5, set5);

        HashSet<String> set6 = new HashSet<>();
        set6.add("rec");
        set6.add("grabRes");
        set6.add("rcs");
        set6.add("i");
        table.put(6, set6);

        HashSet<String> set7 = new HashSet<>();
        set7.add("rcs");   
        set7.add("i");
        table.put(7, set7);
        
        HashSet<String> set8 = new HashSet<>();
        set8.add("rec");
        set8.add("grabNonFileSetRes");
        set8.add("rcs");
        set8.add("i");
        table.put(8, set8);

        HashSet<String> set10 = new HashSet<>();
        set10.add("j");
        set10.add("rec.length");
        set10.add("rec");
        set10.add("length");
        table.put(10, set10);

        HashSet<String> set11 = new HashSet<>();
        set11.add("name");
        set11.add("rec.getName.replace");
        set11.add("j");
        set11.add("rec");
        set11.add("getName.replace");
        set11.add("getName");
        set11.add("replace");
        table.put(11, set11);

        HashSet<String> set12 = new HashSet<>();
        set12.add("rcs");
        set12.add("i");
        table.put(12, set12);

        HashSet<String> set13 = new HashSet<>();
        set13.add("afs");
        set13.add("rcs");
        set13.add("i");
        table.put(13, set13);

        HashSet<String> set14 = new HashSet<>();
        set14.add("rcs");
        set14.add("i");
        set14.add("equals");
        set14.add("afs.getFullpath");
        set14.add("getProj");
        set14.add("afs");
        set14.add("getFullpath");
        table.put(14, set14);

        HashSet<String> set15 = new HashSet<>();
        set15.add("name.afs.getFullpath");
        set15.add("getProj");
        set15.add("name");
        set15.add("afs.getFullpath");
        set15.add("afs");
        set15.add("getFullpath");
        table.put(15, set15);

        HashSet<String> set16 = new HashSet<>();
        set16.add("rcs");
        set16.add("i");
        set16.add("equals");
        set16.add("afs.getFullpath");
        set16.add("getProj");
        set16.add("afs.getPref");
        set16.add("afs");
        set16.add("getFullpath");
        set16.add("getPref");
        table.put(16, set16);
        
        HashSet<String> set17 = new HashSet<>();
        set17.add("pr");
        set17.add("afs.getPref");
        set17.add("getProj");
        set17.add("afs");
        set17.add("getPref");
        table.put(17, set17);

        HashSet<String> set18 = new HashSet<>();
        set18.add("rcs");
        set18.add("i");
        set18.add("equals");
        set18.add("afs.getFullpath");
        set18.add("getProj");
        set18.add("afs.getPref");
        table.put(18, set18);
    
        HashSet<String> set19 = new HashSet<>();
        set19.add("pr");
        table.put(19, set19);

        HashSet<String> set21 = new HashSet<>();
        set21.add("pr");
        set21.add("name");
        table.put(21, set21);
        
        HashSet<String> set24 = new HashSet<>();
        set24.add("name.equalsIgnoreCase");
        set24.add("name");
        set24.add("equalsIgnoreCase");
        table.put(34, set24);

        HashSet<String> set25 = new HashSet<>();
        set25.add("manifests");
        set25.add("i");
        set25.add("rec");
        set25.add("j");
        table.put(25, set25);

        HashSet<String> set29 = new HashSet<>();
        set29.add("manifests");
        set29.add("i");
        table.put(29, set29);
        
        HashSet<String> set30 = new HashSet<>();
        set30.add("manifests");
        set30.add("i");
        table.put(30, set30);
        
        HashSet<String> set33 = new HashSet<>();
        set33.add("manifests");
        table.put(33, set33);
        
        return table;
    }

    private SortedMap<Integer, HashSet<String>> createSmallDummyHashMapForClustering() {
        SortedMap<Integer, HashSet<String>> table = new TreeMap<>();
 
        
        HashSet<String> set1 = new HashSet<>();
        set1.add("manifests");
        table.put(1, set1);

        HashSet<String> set2 = new HashSet<>();
        set2.add("rcs.length");
        table.put(2, set2);

        HashSet<String> set3 = new HashSet<>();
        set3.add("manifests");
        set3.add("i");
        table.put(3, set3);

        HashSet<String> set4 = new HashSet<>();
        set4.add("rcs.length");
        table.put(4, set4);

        HashSet<String> set5 = new HashSet<>();
        set5.add("i");
        table.put(5, set5);
        
        
        return table;
    }

    private void detectSmells(List<Type> allTypes) {
        for(Type type : allTypes) {
            // It is important to detect certain smells at method level first, such as Brain Method
            MethodLevelSmellDetector methodLevelSmellDetector = new MethodLevelSmellDetector();

            for(Method method : type.getMethods()) {
                List<Smell> smells = methodLevelSmellDetector.detect(method);
                method.addAllSmells(smells);
            }

            // Some class-level smell detectors rely on method-level smells as part of their detection
            ClassLevelSmellDetector classLevelSmellDetector = new ClassLevelSmellDetector();
            List<Smell> smells = classLevelSmellDetector.detect(type);
            type.addAllSmells(smells);
        }
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
}
