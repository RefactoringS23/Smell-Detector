package cmu.csdetector.jqual.refactoringOperations;

public class ExtractRefactoring extends RefactoringOperation {


    public ExtractRefactoring () {
    }

    public Cluster getBestCluster () {
        SortedMap<Integer, HashSet<String>> table = getHashMapForClustering();
        Map<String, ASTNode> stringASTNodeMap = getStringASTNodeMap();
        Map<ASTNode, Integer> declaredVars = extractVariableDeclarations(); 
        ClusterManager cm = new ClusterManager(table, stringASTNodeMap, declaredVars); 
        Set<Cluster> blocks = getGrabManifestsBlock();
        Cluster recommendedCluster = cm.getBestCluster(blocks);      

        return recommendedCluster;
    }
}
