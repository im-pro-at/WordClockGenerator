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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

        @Override
        public String toString() {
            return "w="+w+" h="+h;
        }
        
        public void debugPrint(){
            System.out.println("Solution "+this+": ");
            for(int i=0;i<h;i++){
                String s="";
                for(Node n: list[i])
                    s+=n.text+" ";
                
                System.out.println("  "+s);
            }
        }
        
    }
    
    public static class Node{        
        HashSet<Edge> lei = new HashSet<>();        
        HashSet<Edge> leo = new HashSet<>();        
        ArrayList<TimeText> ltt=new ArrayList<>();
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
            String time="";
            for(TimeText tt : ltt)
                time+=tt.getTimeString()+"_";
            return time+"_"+s.toString()+e.toString();
        }
        
        public String getTimes(){
            String time="";
            for(TimeText tt : ltt)
                time+=tt.getTimeString()+"_";
            return time;
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
                cn.ltt.addAll(n.ltt);
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

                JPWordsToGraph.ltt= JPTimeToWords.getTimeTextList().toArray(new TimeText[0]);
                
                setProgress(0,"Init Graph..");
                
                //Build init graph:
                int emaxdelta=0;
                int p=0;
                for(TimeText tt : JPWordsToGraph.ltt){
                    setProgress((int)(0+(25.0*(p++))/JPWordsToGraph.ltt.length),"Init Graph..");
                    String[] words= tt.getText().trim().split("\\s+");
                    emaxdelta=Math.max(emaxdelta, words.length);
                    Node last=g.start;
                    for(String word : words){
                        //Try to find node with this text:
                        Node temp=null;                
                        for(Edge e: last.leo){
                            if(e.e.text.equals(word)){
                                temp=e.e;
                                temp.ltt.add(tt);
                                break;
                            }
                        }
                        if(temp==null){
                            temp= new Node();
                            temp.ltt.add(tt);
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

                            //Find matches:
                            for(Node ns:nsetstart){
                                for(Node nd:nsetdelta){
                                    if(ns!=nd && ns.text.equals(nd.text)){
                                        //Test if there will be a cylce after the merge => is nd reachable from ns:
                                        HashSet<Node> ndone= new HashSet<>();
                                        Stack<Node> ntodo=new Stack<>();
                                        ntodo.push(ns);

                                        while(!ntodo.empty()){
                                            Node n=ntodo.pop();
                                            if(!ndone.contains(n)){
                                                ndone.add(n);
                                                for(Edge e:n.leo){
                                                    ntodo.push(e.e);
                                                }
                                            }
                                        }

                                        if(ndone.contains(nd)){
                                            //Cycle
                                            continue;
                                        }

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
                                        for(TimeText tt : nd.ltt){
                                            ns.ltt.add(tt);
                                        }
                                            
                                        //remode nd
                                        g.nodes.remove(nd);
                                        
                                        this.publish(g.cloneGraph());

                                        Thread.sleep(10);
                                        
                                        change=true;
                                        break;                                    

                                    }
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
                
                int minwidth=0;
                int cletters=0;
                for(Node n:g.nodes){
                    minwidth=Math.max(minwidth, n.text.length());
                    cletters+=n.text.length()+1;
                }
                
                
                for(int w=minwidth;w<cletters;w++){
                    setProgress((int)(50+(50.0*(w-minwidth))/(cletters-minwidth)),"Finding Solutions");
                    boolean doneinoneline=false;
                    for(int h=1;h<g.nodes.size();h++){
                        //init working Graph and make start as done!
                        HashSet<Node> ndone = new HashSet<>();
                        ndone.add(g.start);

                        //init solution structure
                        ArrayList<Node>[] r= new ArrayList[h];
                        int[] rl= new int[h];
                        for(int i=0;i<h;i++){
                            r[i]=new ArrayList<>();
                            rl[i]=0;
                        }
                        int lineindex=0;
                        
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
                            boolean[] needspace= new boolean[nfrees.size()];
                            
                            //Calc length after adding the free nodes to the line:
                            for(int i=0;i<nfrees.size();i++){
                                nfreesl[i]=rl[lineindex]+nfrees.get(i).text.length();
                                //Check if space is needed:
                                if(!r[lineindex].isEmpty()){
                                    for(Edge e: r[lineindex].get(r[lineindex].size()-1).leo){
                                        if(e.e==nfrees.get(i)){
                                            needspace[i]=true;
                                            nfreesl[i]++;
                                        }
                                    }
                                }
                            }
                            
                            //Decide what to add:
                            int toadd=-1;                            
                            int max=0;
                            for(int i=0;i<nfrees.size();i++){
                                if(needspace[i]==false && nfreesl[i]<=w){
                                    if(max<nfrees.get(i).text.length()){
                                        toadd=i;
                                        max=nfrees.get(i).text.length();
                                    }
                                }
                            }
                            if(toadd==-1)
                            {
                                //Try to add with space
                                max=0;
                                for(int i=0;i<nfrees.size();i++){
                                    if(needspace[i]==true && nfreesl[i]<=w){
                                        if(max<nfrees.get(i).text.length()){
                                            toadd=i;
                                            max=nfrees.get(i).text.length();
                                        }
                                    }
                                }
                            }
                            
                            
                            if(toadd==-1){
                                //No node found => line done!
                                lineindex++; 
                            }
                            else{
                                //add node
                                r[lineindex].add(nfrees.get(toadd));
                                rl[lineindex]=nfreesl[toadd];
                                //make node as done
                                ndone.add(nfrees.get(toadd));
                                
                            }
                            
                        }
                        
                        //Print solution
                        if((ndone.size()-1) == g.nodes.size())
                        {
                            Solution s= new Solution();
                            s.h=h;
                            s.w=w;
                            s.list=r;
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
                
                
                
                JPWordsToGraph.graph=g;
                JPWordsToGraph.solutions= solutions.toArray(new Solution[0]);
                return g;
            }

            @Override
            protected void done(Graph rvalue, Exception ex, boolean canceled) {
                jTBCalc.setEnabled(true);
                display(rvalue);
                jPB.setValue(100);
                jPB.setString("Done");
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
