/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufc.sma.reputacao;

import jade.core.AID;
import jade.util.leap.Serializable;

/**
 *
 * @author spectrus
 */
public class Reputation implements Serializable{
    private float reputation;
    private AID agent;
    
    public Reputation(float reputation, AID agent){
        this.reputation = reputation;
        this.agent = agent;
    }
    
    public float getReputation(){
        return this.reputation;
    }
    
    public AID getAgent(){
        return this.agent;
    }
    
    public void setAgent(AID agent){
        this.agent = agent;
    }
    
    public void setReputation(float reputation){
        this.reputation = reputation;
    }
}
