/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.impro.wordclockgenerator;

import java.io.Serializable;

/**
 *
 * @author patrick
 */
public class TimeText implements Serializable{
    private int h;
    private int m;
    private String text;

    public TimeText(int h, int m, String text) {
        this.h = h;
        this.m = m;
        this.text = text;
    }

    public int getH() {
        return h;
    }

    public int getM() {
        return m;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.h;
        hash = 59 * hash + this.m;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimeText other = (TimeText) obj;
        if (this.h != other.h) {
            return false;
        }
        if (this.m != other.m) {
            return false;
        }
        return true;
    }
    
    
    @Override
    public String toString() {
        String time = getTimeString();
        if(!text.equals("")){
            time+=" -> ";
            time+=text;
        }
        return time;

    }
    
    public String getTimeString(){
        String time="";
        time+=String.format("%02d", h);
        time+=":"+String.format("%02d", m);
        return time;
    }
    
    public void setText(String text){
        this.text=text;
    }

    public String getText() {
        return text;
    }

    
    
}
