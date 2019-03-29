package br.com.goup.snkcustomevents.financial;

import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

public class ActionsTef implements ScheduledAction{

    @Override
    public void onTime(ScheduledActionContext contexto) {
        try{
        	UpdateTef updateTef = new UpdateTef();
            updateTef.actionSendDataTef();
        }
        catch (Exception e){ }
    }
}