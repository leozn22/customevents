package br.com.goup.snkcustomevents.events;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;

/**
 * Authors: guilherme.alves, andre.santos Date: 21/11/2018 Reason: Event created
 * to log (AD_TGFLOGEDI) every time there is an EDI return for determining NUFIN
 */

public class EDIReturnLogEvent implements EventoProgramavelJava {

	/**
	 * Method to create new register on table AD_TGFLOGEDI
	 * 
	 * @param persistenceEvent persistent object of the financial
	 * @throws Exception
	 */
	private static void registerReturnLog(PersistenceEvent persistenceEvent) throws Exception {
		JapeWrapper logDAO = JapeFactory.dao("AD_TGFLOGEDI");
		DynamicVO logVO = (DynamicVO) persistenceEvent.getVo();

		if ("O".equals(logVO.asString("TIPO"))) {
			FluidCreateVO creLog = logDAO.create();
			creLog.set("NUFIN", logVO.asBigDecimal("NUFIN"));
			creLog.set("CODUSU", logVO.asBigDecimal("CODUSU"));
			creLog.set("DHMOV", TimeUtils.getNow());
			creLog.set("TIPO", "RETORNO");
			creLog.set("NUMREMANT", null);
			creLog.set("NUMREMATU", null);
			creLog.set("MENSAGEM", logVO.asString("DESCRICAO"));
			creLog.save();
		}
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

		/**
		 * The code below not work because table TSIOBCLOC is not managed by Sankhya
		 * listeners. We create a trigger to resolve the case BH_TRG_LOGEDI_RETORNO.
		 */

		try {
			registerReturnLog(persistenceEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
		// TODO Auto-generated method stub

	}

}
