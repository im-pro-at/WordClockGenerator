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
public class TimeText implements Serializable, Cloneable{
    private boolean time;
    private int h;
    private int m;
    private int index;
    private String text;

    public TimeText(int h, int m, String text) {
        this.time = true;
        this.h = h;
        this.m = m;
        this.index = -1;
        this.text = text;
    }
    public TimeText(int index, String text) {
        this.time = false;
        this.h = -1;
        this.m = -1;
        this.index = index;
        this.text = text;
    }
    
    public boolean isTime(){
        return time;
    }

    public int getH() {
        return h;
    }

    public int getM() {
        return m;
    }
    public int getIndex()
    {
        return index;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.h;
        hash = 59 * hash + this.m;
        hash = 59 * hash + this.index;
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
        if (this.time != other.time) {
            return false;
        }
        if(this.time){
            if (this.h != other.h) {
                return false;
            }
            if (this.m != other.m) {
                return false;
            }
        }
        else
        {
            if (this.index != other.index) {
                return false;
            }
        }
        return true;
    }
    
    
    @Override
    public String toString() {
        String info;
        if(isTime())
        {
            info = getTimeString();        
        }
        else
        {
            info = getIndexString();                
        }
        if(!this.text.equals("")){
            info+=" -> ";
            info+=this.text;
        }
        return info;

    }
    
    public String getTimeString(){
        String time="";
        time+=String.format("%02d", h);
        time+=":"+String.format("%02d", m);
        return time;
    }

    public String getIndexString(){
        return "Custom_"+String.format("%02d", index);
    }
    
    public void setText(String text){
        this.text=text;
    }

    public String getText() {
        return text;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); 
    }



}
