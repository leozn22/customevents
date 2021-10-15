package br.com.goup.snkcustomevents;

/**
 * @author Renato Corrêa
 * Esta classe trata as formas de conexões com as APIs de integração do Sankhya com as bases de dados
 * do SQL Server e Mysql, tanto de produção, quanto de teste.
 * 
 * Sendo assim, é possível forçar uma URL de acordo com as opções abaixo, para poder trabalhar com as
 * bases de dados de diferentes formas:
 * 	- LocalTest: Base de teste do Sankhya com base de teste do SQL Server e Mysql. Ambiente de desenvolvimento
 *  - ProductionTest: Base de teste do Sankhya com base de produção do SQL Server e Mysql. Homologação (Servidor do APP)
 *  - AllTest: Base de teste do Sankhya com base de teste do SQL Server e Mysql. Homologação (Servidor do APP)
 *  - Production: Base de produção do Sankhya com base de produção do SQL Server e Mysql. Ambiente de Produção
 */
abstract public class SnkIntegrationsApi {

	protected String urlApi;
	protected boolean exigeAutenticacao = false;

	protected SnkIntegrationsApi() { 
		this.forceUrl("AllTest"); // Opções: LocalTest, ProductionTest, AllTest, Production
	}
	
	/**
	 * @param String typeUrl, opções: LocalTest, ProductionTest, AllTest, Production
	 * @return String
	 */
	protected String forceUrl(String typeUrl) {
		
		switch (typeUrl) {
			case "LocalTest":
				this.urlApi = this.getUrlLocalTest();
				break;
	
			case "ProductionTest":
				this.urlApi = this.getUrlProductionTest();
				break;
	
			case "AllTest":
				this.urlApi = this.getUrlAllTest();
				break;
	
			case "Production":
				this.urlApi = this.getUrlProduction();
				break;
		}
		
		return this.urlApi;
	}
	
	// Teste Local
	protected String getUrlLocalTest() {
		this.urlApi = "http://127.0.0.1:8080"; // Teste Local
		if(!this.exigeAutenticacao) {
			this.urlApi+= "/api";
		}
		return this.urlApi;
	}
	
	// Híbrido (base de teste Sankhya, base de produção: Mysql e SQL Server)
	protected String getUrlProductionTest() {
		this.urlApi = "http://api-odin-hom.sa-east-1.elasticbeanstalk.com:8080";
		if(!this.exigeAutenticacao) {
			this.urlApi+= "/api";
		}
		return this.urlApi;
	}
	
	// Todas as bases de teste
	protected String getUrlAllTest() {
//		this.urlApi = "https://api-odin-dev.azurewebsites.net";
		this.urlApi = "http://sgw.zapgrafica.com.br:8081";
		if(!this.exigeAutenticacao) {
			this.urlApi+= "/api";
		}
		return this.urlApi;
	}
	
	// Todas as bases de produção
	protected String getUrlProduction() {
		this.urlApi = "https://api-odin.azurewebsites.net";
		if(!this.exigeAutenticacao) {
			this.urlApi+= "/api";
		}
		return this.urlApi;
	}

	protected String getUrl(){
		return "https://api-odin.azurewebsites.net";
	}
}
