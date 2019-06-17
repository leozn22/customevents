package br.com.goup.snkcustomevents.orders;

import java.sql.Timestamp;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;

public class SetorItem implements EventoProgramavelJava {	
	
	private void insertStatusItem(PersistenceEvent persistenceEvent) throws Exception {		
			
		if (persistenceEvent.getModifingFields().isModifing("TZASTATUS") 
				&& persistenceEvent.getEntityProperty("TZANUITEM") != null) {
			
			JapeWrapper itemDAO = JapeFactory.dao("AD_ITEST");
			FluidCreateVO createVO = itemDAO.create();
			createVO.set("TZANUITEM", persistenceEvent.getEntityProperty("TZANUITEM"));
			createVO.set("DHSTATUS", new Timestamp(System.currentTimeMillis()));
			createVO.set("TZASTATUS", persistenceEvent.getEntityProperty("TZASTATUS") == null ? "AP" : persistenceEvent.getEntityProperty("TZASTATUS"));
			createVO.save();		
		}
	}
	
	@Override
	public void afterDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterUpdate(PersistenceEvent arg0) throws Exception {
		insertStatusItem(arg0);		
	}

	@Override
	public void beforeCommit(TransactionContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeDelete(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeInsert(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeUpdate(PersistenceEvent arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
