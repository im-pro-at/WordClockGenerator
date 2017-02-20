/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wordclockgenerator;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import javax.swing.GroupLayout;

/**
 *
 * @author patrick
 */
public class JPWordsToGraph extends javax.swing.JPanel {

    static private Solution[] solutions=null;
    static private Graph graph=null;
    static private TimeText[] ltt=null;
    
    public static Solution[] getSolutions(){
        return solutions;
    }

    public static Graph getGraph() {
        return graph;
    }

    public static TimeText[] getTimeTextList() {
        return ltt;
    }

    
    
    public static class Solution{
        int w;
        int h;
        ArrayList<Node>[] list;  
        ArrayList<Integer>[] overlap;  

        @Override
        public String toString() {
            return "w="+w+" h="+h;
        }
        
        public void debugPrint(){
            System.out.println("Solution "+this+": ");
            for(int i=0;i<h;i++){
                String s="";
                for(int j=0;j<list[i].size();j++){
                    s+=" ("+ (0-overlap[i].get(j)) +") "+list[i].get(j).text;
                }
                System.out.println("  "+s);
            }
        }
        
    }
    
    public static class Node{        
        HashSet<Edge> lei = new HashSet<>();        
        HashSet<Edge> leo = new HashSet<>();     
        Map<TimeText,String> mtt= new HashMap<>();

        String text="";

        @Override
        public String toString() {
            return text+"_"+super.toString(); 
        }

        public Node(String text) {
            this.text=text;
        }

        public Node() {
        }
        
        
        public boolean reachable(Node nreach){
            if(this==nreach){ //same node?
                return true;
            }
            HashSet<Node> ndone= new HashSet<>();
            Stack<Node> ntodo=new Stack<>();
            ntodo.push(this);

            while(!ntodo.empty()){
                Node n=ntodo.pop();
                if(!ndone.contains(n)){
                    if(n==nreach){
                        return true;
                    }
                    ndone.add(n);
                    for(Edge e:n.leo){
                        ntodo.push(e.e);
                    }
                }
            }
            return false;
        }
        
        
    }
    public static class Edge{
        Node s;
        Node e;

        public Edge(Node s, Node e) {
            this.s = s;
            this.e = e;
        }
        
        @Override
        public String toString() {
            return s.toString()+e.toString();
        }
    }
    
    
    public static class Graph{
        Node start= new Node("$START$");
        Node end= new Node("$END$");

        ArrayList<Node> nodes= new ArrayList<>();

        protected Graph cloneGraph() {
            Graph g= new Graph();
            HashMap<Node,Node> clist= new HashMap<>();
            clist.put(this.start, g.start);
            clist.put(this.end, g.end);
            
            for(Node n : this.nodes){
                Node cn= new Node(n.text);
                clist.put(n, cn);
                cn.mtt= new HashMap<>(n.mtt);
                g.nodes.add(cn);
            }
            for(Edge e :this.start.leo){
                Edge ce= new Edge(clist.get(e.s), clist.get(e.e));
                clist.get(e.s).leo.add(ce);
                clist.get(e.e).lei.add(ce);
            }
            for(Node n : this.nodes){
                for(Edge e :n.leo){
                    Edge ce= new Edge(clist.get(e.s), clist.get(e.e));
                    clist.get(e.s).leo.add(ce);
                    clist.get(e.e).lei.add(ce);
                }
            }            
            return g;
        }

    }
    
    VisualizationViewer<Node,Edge> vv= new VisualizationViewer<>(new FRLayout<>(new DirectedSparseGraph<>()));   
    Graph displaygraph = null;
    Set<Node> displaymake= null;
    private void display(Graph graph) {
        displaygraph=graph;
        edu.uci.ics.jung.graph.Graph<Node,Edge> g = new DirectedSparseGraph<>();
        g.addVertex(graph.start);
        g.addVertex(graph.end);
        for(Node node : graph.nodes){
            g.addVertex(node);
        }
        for(Edge e: graph.start.leo){
            g.addEdge(e, e.s,e.e);                
        }
        for(Node node : graph.nodes){
            for(Edge e: node.leo){
                g.addEdge(e, e.s,e.e);                
            }
        }
        Dimension d= jPgraph.getSize();
        Layout<Node,Edge> l= new FRLayout<>(g);
        l.setSize(d);
        l.setLocation(graph.start, new Point2D.Double(50,d.height/2));
        l.setLocation(graph.end, new Point2D.Double(d.width-50,d.height/2));
        l.lock(graph.start,true);
        l.lock(graph.end,true);
        vv.setGraphLayout(l);
        vv.setPreferredSize(d);
    }

    
    
    /**
     * Creates new form JPWordsToMatrix
     */
    public JPWordsToGraph() {
        initComponents();
        vv.setBackground(new java.awt.Color(255, 255, 255));
        vv.getRenderContext().setVertexLabelTransformer((Node i) -> {
            if(displaygraph==null || i==displaygraph.start || i==displaygraph.end)
                return "";
            return i.text;
        });
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        vv.getRenderContext().setVertexShapeTransformer((Node i) -> {
            if(displaygraph==null || i==displaygraph.start || i==displaygraph.end)
                return new Rectangle2D.Double(-5,-5,10,10);
            String t = i.text;
            int width1 = t.length() * 10; // 12.0 is approx size of letter
            Ellipse2D e = new Ellipse2D.Double(-(width1 / 2), -12.5, width1, 25);
            return e;
        });
        vv.getRenderContext().setVertexFillPaintTransformer((Node i) -> {
            if(displaygraph==null || i==displaygraph.start || i==displaygraph.end)
                return Color.BLACK;
            Set<Node> mark= this.displaymake;
            if(mark!=null && mark.contains(i))
                return Color.LIGHT_GRAY;
            return Color.WHITE;
        });
        DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        
        jPgraph.removeAll();
        GroupLayout jPanelLayout = (GroupLayout)jPgraph.getLayout();
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(vv, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(vv, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPgraph.validate();
        
    }

    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTBCalc = new javax.swing.JToggleButton();
        jPB = new javax.swing.JProgressBar();
        jPgraph = new javax.swing.JPanel();

        jTBCalc.setText("Calc");
        jTBCalc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTBCalcActionPerformed(evt);
            }
        });

        jPB.setString("");
        jPB.setStringPainted(true);

        javax.swing.GroupLayout jPgraphLayout = new javax.swing.GroupLayout(jPgraph);
        jPgraph.setLayout(jPgraphLayout);
        jPgraphLayout.setHorizontalGroup(
            jPgraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPgraphLayout.setVerticalGroup(
            jPgraphLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 256, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPgraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTBCalc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPB, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPgraph, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPB, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jTBCalc)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    
    
    
    
    private void jTBCalcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTBCalcActionPerformed

        MySwingWorker<Graph,Graph> sw = new MySwingWorker<Graph,Graph>() {

            @Override
            protected Graph doInBackground() throws Exception {

                Graph g = new Graph();

                TimeText[] ltt= JPTimeToWords.getTimeTextList().toArray(new TimeText[0]);
                
                setProgress(0,"Init Graph..");
                
                //Build init graph:
                int emaxdelta=0;
                int p=0;
                for(TimeText tt : ltt){
                    setProgress((int)(0+(25.0*(p++))/ltt.length),"Init Graph..");
                    String[] words= tt.getText().trim().split("\\s+");
                    emaxdelta=Math.max(emaxdelta, words.length);
                    Node last=g.start;
                    for(String word : words){
                        //Try to find node with this text:
                        Node temp=null;                
                        for(Edge e: last.leo){
                            if(e.e.text.equals(word)){
                                temp=e.e;
                                temp.mtt.put(tt, word);
                                break;
                            }
                        }
                        if(temp==null){
                            temp= new Node();
                            temp.mtt.put(tt, word);
                            g.nodes.add(temp);
                            temp.text=word;
                            Edge e= new Edge(last,temp);
                            last.leo.add(e);
                            temp.lei.add(e);
                            
                            this.publish(g.cloneGraph());
                            Thread.sleep(10);
                        }
                        last=temp;
                    }
                    Edge e= new Edge(last,g.end);
                    last.leo.add(e);
                    g.end.lei.add(e);
                }

                this.publish(g.cloneGraph());
                
                setProgress(25,"Find maching nodes..");
                float cnodes=g.nodes.size();
                //Find maching nodes
                emaxdelta+=2; //START + END
                boolean change=true;
                while(change)
                {
                    setProgress((int)(25+ 25*(1-(float)g.nodes.size()/cnodes)),"Find maching nodes..");
                    change=false;
                    for(int aechanges=1;aechanges<g.nodes.size();aechanges++){
                        for(boolean reverse:new boolean[]{false,true}){
                            for(int edelta=0;edelta<emaxdelta;edelta++){
                                for(int estart=0;estart<emaxdelta-edelta;estart++){
                                    HashSet<Node> nsetstart=new HashSet<>();
                                    nsetstart.add(g.start);
                                    //generate two note sets to compair
                                    for(int i=0;i<estart;i++){
                                        HashSet<Node> temp=new HashSet<>();
                                        for(Node n:nsetstart){
                                            for(Edge e: n.leo){
                                                temp.add(e.e);
                                            }
                                        }
                                        nsetstart=temp;
                                    }
                                    HashSet<Node> nsetdelta=nsetstart;
                                    for(int i=0;i<edelta;i++){
                                        HashSet<Node> temp=new HashSet<>();
                                        for(Node n:nsetdelta){
                                            for(Edge e: n.leo){
                                                temp.add(e.e);
                                            }
                                        }
                                        nsetdelta=temp;
                                    }

                                    nsetstart.remove(g.start);
                                    nsetstart.remove(g.end);
                                    nsetdelta.remove(g.start);
                                    nsetdelta.remove(g.end);

                                    Node ns=null, nd=null;

                                    //Find matches:
                                    for(Node tns:nsetstart){
                                        for(Node tnd:nsetdelta){
                                            if(reverse){
                                                Node t=tns;
                                                tns=tnd;
                                                tnd=t;
                                            }
                                            if(tns.text.contains(tnd.text)){
                                                //Will there be a cicle after joining ns and nd?
                                                if(tns.reachable(tnd) || tnd.reachable(tns)){
                                                    continue;
                                                }
                                                //Will there be more new ages then allowed:
                                                int anew=0;
                                                for(Edge end: tnd.lei){
                                                    boolean exists=false;
                                                    for(Edge ens: tns.lei){
                                                        if(end.s==ens.s){
                                                            exists=true;
                                                            break;
                                                        }
                                                    }
                                                    if(!exists){
                                                        anew++;
                                                    }
                                                }
                                                for(Edge end: tnd.leo){
                                                    boolean exists=false;
                                                    for(Edge ens: tns.leo){
                                                        if(end.e==ens.e){
                                                            exists=true;
                                                            break;
                                                        }
                                                    }
                                                    if(!exists){
                                                        anew++;
                                                    }
                                                }
                                                if(anew>aechanges){
                                                    continue;
                                                }
                                                
                                                ns=tns;
                                                nd=tnd;
                                                break;
                                            }
                                        }
                                        if(ns!=null) break;
                                    }


                                    if(ns!=null){
                                        //No cylce matche! merge ns and nd ==> delete nd
                                        //Add edges from nd to ns
                                        for(Edge nd_eo:nd.leo){
                                            nd_eo.s=ns;
                                            nd_eo.e.lei.remove(nd_eo);                                    
                                            boolean exists=false;
                                            for(Edge ns_eo:ns.leo){
                                                if(ns_eo.e==nd_eo.e){
                                                    //Edge exits
                                                    exists=true;
                                                    break;                                                    
                                                }
                                            }                                            
                                            if(!exists){
                                                //readd Edge
                                                nd_eo.s.leo.add(nd_eo);
                                                nd_eo.e.lei.add(nd_eo);
                                            }
                                        }
                                        for(Edge nd_ei:nd.lei){
                                            nd_ei.e=ns;
                                            nd_ei.s.leo.remove(nd_ei);                                    
                                            boolean exists=false;
                                            for(Edge ns_ei:ns.lei){
                                                if(ns_ei.s==nd_ei.s){
                                                    //Edge exits
                                                    exists=true;
                                                    break;                                                    
                                                }
                                            }                                            
                                            if(!exists){
                                                //readd Edge
                                                nd_ei.s.leo.add(nd_ei);
                                                nd_ei.e.lei.add(nd_ei);
                                            }
                                        }


                                        //transfer tts
                                        for(Map.Entry<TimeText,String> e : nd.mtt.entrySet()){
                                            ns.mtt.put(e.getKey(),e.getValue());
                                        }

                                        //remode nd
                                        g.nodes.remove(nd);

                                        this.publish(g.cloneGraph());

                                        Thread.sleep(10);

                                        change=true;
                                        break;                                    

                                    }


                                    if(change) break;
                                }
                                if(change) break;
                            }
                            if(change) break;
                        }
                        if(change) break;
                    }
                }
                publish(g.cloneGraph());

                setProgress(50,"Finding Solutions");
                ArrayList<Solution> solutions= new ArrayList<>();
                
                if(g.nodes.size()<2){
                    setProgress(50,"Graph has less than two node cannot find a mapping!");                    
                    return g;
                }
                
                int minwidth=0;
                int cletters=0;
                for(Node n:g.nodes){
                    minwidth=Math.max(minwidth, n.text.length());
                    cletters+=n.text.length()+1;
                }
                
                Random rand= new Random(0);
                for(int w=minwidth;w<cletters;w++){
                    setProgress((int)(50+(50.0*(w-minwidth))/(cletters-minwidth)),"Finding Solutions");
                    boolean doneinoneline=false;
                    for(int h=1;h<g.nodes.size();h++){
                        //init working Graph and make start as done!
                        HashSet<Node> ndone = null;
                        
                        int lineindex=0;
                        ArrayList<Node>[] r=null;
                        ArrayList<Integer>[] ro=null;
                        for(int itry=0;itry<1000;itry++){
                            //init solution structure
                            ndone = new HashSet<>();
                            ndone.add(g.start);
                            r= new ArrayList[h];
                            ro= new ArrayList[h];
                            int[] rl= new int[h];
                            for(int i=0;i<h;i++){
                                r[i]=new ArrayList<>();
                                ro[i]= new ArrayList<>();
                                rl[i]=0;
                            }
                            lineindex=0;

                            while( (ndone.size()-1) <  g.nodes.size() && lineindex<h){
                                //Find free nodes to add
                                ArrayList<Node> nfrees= new ArrayList<>();
                                for(Node n : g.nodes){
                                    if(!ndone.contains(n)){
                                        boolean free=true;
                                        for(Edge e :n.lei){
                                            if(!ndone.contains(e.s)){
                                                free=false;
                                                break;
                                            }
                                        }
                                        if(free){
                                            nfrees.add(n);
                                        }
                                    }
                                }

                                int[] nfreesl= new int[nfrees.size()];
                                int[] nfreeso= new int[nfrees.size()];
                                //Calc length after adding the free nodes to the line:
                                for(int i=0;i<nfrees.size();i++){
                                    //Check if space is needed:
                                    boolean needspace=false;
                                    if(!r[lineindex].isEmpty()){
                                        for(Edge e: r[lineindex].get(r[lineindex].size()-1).leo){
                                            if(e.e==nfrees.get(i)){
                                                needspace=true;
                                                break;
                                            }
                                        }
                                    }
                                    int overlapping=0;
                                    if(needspace){
                                        overlapping=-1;
                                    }
                                    else{
                                        if(!r[lineindex].isEmpty()){
                                            String s1=r[lineindex].get(r[lineindex].size()-1).text;
                                            String s2=nfrees.get(i).text;
                                            for(int o=1;o<Math.min(s1.length(), s2.length());o++){
                                                boolean over=true;
                                                for(int l=0;l<o;l++){
                                                    if(s1.charAt(s1.length()-o+l)!=s2.charAt(l)){
                                                        over=false;
                                                        break;
                                                    }
                                                }
                                                if(over){
                                                    overlapping=o;
                                                }
                                                else{
                                                    break;
                                                }                                                    
                                            }
                                        }
                                    }
                                    nfreeso[i]=overlapping;
                                    nfreesl[i]=rl[lineindex]+nfrees.get(i).text.length()-overlapping;
                                }

                                //What can be add:
                                ArrayList<Integer> toadd= new ArrayList<>();                            
                                for(int i=0;i<nfrees.size();i++){
                                    if(nfreesl[i]<=w){
                                        toadd.add(i);
                                    }
                                }

                                if(toadd.isEmpty()){
                                    //No node found => line done!
                                    lineindex++; 
                                }
                                else{
                                    int index= rand.nextInt(toadd.size());
                                    //add node
                                    r[lineindex].add(nfrees.get(toadd.get(index)));
                                    ro[lineindex].add(nfreeso[toadd.get(index)]);
                                    rl[lineindex]=nfreesl[toadd.get(index)];
                                    //make node as done
                                    ndone.add(nfrees.get(toadd.get(index)));
                                }

                            }
                            if((ndone.size()-1) == g.nodes.size()) {
                                break; //Has soulution!!!
                            }
                        }
                        //Print solution
                        if((ndone.size()-1) == g.nodes.size())
                        {
                            Solution s= new Solution();
                            s.h=h;
                            s.w=w;
                            s.list=r;
                            s.overlap=ro;
                            solutions.add(s);
                        }
                        
                        if(lineindex<h){
                            if(h==1)
                                doneinoneline=true;
                            break;
                        }
                    }
                    if(doneinoneline)
                        break;
                }
                
                
                JPWordsToGraph.ltt=ltt;
                JPWordsToGraph.graph=g;
                JPWordsToGraph.solutions= solutions.toArray(new Solution[0]);
                setProgress(100,"Done");                    
                return g;
            }

            @Override
            protected void done(Graph rvalue, Exception ex, boolean canceled) {
                jTBCalc.setEnabled(true);
                display(rvalue);
            }

            @Override
            protected void process(List<Graph> chunks) {
                display(chunks.get(chunks.size()-1));
            }

            @Override
            protected void progress(int progress, String message) {
                jPB.setValue(progress);
                jPB.setString(message);
            }
            
            
        };
        sw.execute();
        jTBCalc.setEnabled(false);
        
    }//GEN-LAST:event_jTBCalcActionPerformed

    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar jPB;
    private javax.swing.JPanel jPgraph;
    private javax.swing.JToggleButton jTBCalc;
    // End of variables declaration//GEN-END:variables
}
