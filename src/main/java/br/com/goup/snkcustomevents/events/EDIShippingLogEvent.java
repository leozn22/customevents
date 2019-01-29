package br.com.goup.snkcustomevents.events;

import java.math.BigDecimal;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;

/**
 * Authors: guilherme.alves, andre.santos Date: 21/11/2018 Reason: Event created
 * for logging (AD_TGFLOGEDI) every time that the dispatcher's number is
 * modified.
 */

public class EDIShippingLogEvent implements EventoProgramavelJava {

	/**
	 * Method to register log of shipping EDI on table AD_TGFLOGEDI
	 * 
	 * @param persistenceEvent persistent object of the financial
	 * @throws Exception
	 */
	private static void registerShippingLog(PersistenceEvent persistenceEvent) throws Exception {
		JapeWrapper logDAO = JapeFactory.dao("AD_TGFLOGEDI");
		ModifingFields modifingFields = persistenceEvent.getModifingFields();
		DynamicVO finVO = (DynamicVO) persistenceEvent.getVo();

		FluidCreateVO creLog = logDAO.create();
		creLog.set("NUFIN", finVO.asBigDecimal("NUFIN"));
		creLog.set("CODUSU", AuthenticationInfo.getCurrent().getUserID());
		creLog.set("DHMOV", TimeUtils.getNow());
		creLog.set("TIPO", "ENVIO");

		if (modifingFields.getOldValue("NUMREMESSA") != null)
			creLog.set("NUMREMANT", modifingFields.getOldValue("NUMREMESSA").toString());

		creLog.set("NUMREMATU", modifingFields.getNewValue("NUMREMESSA").toString());

		if ("".equals(modifingFields.getNewValue("NUMREMESSA"))
				|| ((BigDecimal) modifingFields.getNewValue("NUMREMESSA")).compareTo(BigDecimal.ZERO) == 0) {
			creLog.set("MENSAGEM", "NÚMERO DE REMESSA ZERADO/LIMPO");
		} else {
			creLog.set("MENSAGEM", "REMESSA GERADA OU MODIFICADA");
		}
		creLog.save();

	}

	@Override
	public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeCommit(TransactionContext persistenceEvent) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
		try {
			ModifingFields modifingFields = persistenceEvent.getModifingFields();
			if (modifingFields.isModifing("NUMREMESSA")
					|| ((BigDecimal) persistenceEvent.getEntityProperty("NUMREMESSA"))
							.compareTo(BigDecimal.ZERO) == 0) {
				registerShippingLog(persistenceEvent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
