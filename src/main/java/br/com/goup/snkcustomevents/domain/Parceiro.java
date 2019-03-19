package br.com.goup.snkcustomevents.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.print.attribute.DateTimeSyntax;

import com.google.gson.Gson;

import br.com.sankhya.jape.vo.DynamicVO;

public class Parceiro {
	
	private BigDecimal codigoParceiroSankhya;
	private String tipoPessoa;
	private String classificacaoICMS;
	private BigDecimal codigoIntegracao;
	private String razaoSocial;
	private String nomeParceiro;
	private String cpf;
	private String ie;
	private String im;
	private String ativo;
	private String cliente;
	private String usuario;
	private String fornecedor;
	private String transportadora;
	private String fax;
	private String site;
	private BigDecimal limiteCredito;
	private String dataUltimoContato;
	private String classeParceiro;
	private String observacoes;
	private Date dataCadastro;
	private DateTimeSyntax dataAlteracao;
	private String email;
	private String telefone;
	private BigDecimal codigoEndereco;
	private String tipoEndereco;
	private String endereco;
	private String numeroEndereco;
	private String complemento;
	private BigDecimal codigoBairro;
	private String bairro;
	private BigDecimal codigoCidade;
	private String cidade;
	private String uf;
	private String cep;
	private String pontoReferencia;
	private BigDecimal codigoPerfil;
	private String perfil;
	private String retemIss;
	private String cadastroLiberado;
	public BigDecimal getCodigoParceiroSankhya() {
		return codigoParceiroSankhya;
	}
	public void setCodigoParceiroSankhya(BigDecimal codigoParceiroSankhya) {
		this.codigoParceiroSankhya = codigoParceiroSankhya;
	}
	public String getTipoPessoa() {
		return tipoPessoa;
	}
	public void setTipoPessoa(String tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}
	public String getClassificacaoICMS() {
		return classificacaoICMS;
	}
	public void setClassificacaoICMS(String classificacaoICMS) {
		this.classificacaoICMS = classificacaoICMS;
	}
	public BigDecimal getCodigoIntegracao() {
		return codigoIntegracao;
	}
	public void setCodigoIntegracao(BigDecimal codigoIntegracao) {
		this.codigoIntegracao = codigoIntegracao;
	}
	public String getRazaoSocial() {
		return razaoSocial;
	}
	public void setRazaoSocial(String razaoSocial) {
		this.razaoSocial = razaoSocial;
	}
	public String getNomeParceiro() {
		return nomeParceiro;
	}
	public void setNomeParceiro(String nomeParceiro) {
		this.nomeParceiro = nomeParceiro;
	}
	public String getCpf() {
		return cpf;
	}
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	public String getIe() {
		return ie;
	}
	public void setIe(String ie) {
		this.ie = ie;
	}
	public String getIm() {
		return im;
	}
	public void setIm(String im) {
		this.im = im;
	}
	public String getAtivo() {
		return ativo;
	}
	public void setAtivo(String ativo) {
		this.ativo = ativo;
	}
	public String getCliente() {
		return cliente;
	}
	public void setCliente(String cliente) {
		this.cliente = cliente;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public String getFornecedor() {
		return fornecedor;
	}
	public void setFornecedor(String fornecedor) {
		this.fornecedor = fornecedor;
	}
	public String getTransportadora() {
		return transportadora;
	}
	public void setTransportadora(String transportadora) {
		this.transportadora = transportadora;
	}
	public String getFax() {
		return fax;
	}
	public void setFax(String fax) {
		this.fax = fax;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public BigDecimal getLimiteCredito() {
		return limiteCredito;
	}
	public void setLimiteCredito(BigDecimal limiteCredito) {
		this.limiteCredito = limiteCredito;
	}
	public String getDataUltimoContato() {
		return dataUltimoContato;
	}
	public void setDataUltimoContato(String dataUltimoContato) {
		this.dataUltimoContato = dataUltimoContato;
	}
	public String getClasseParceiro() {
		return classeParceiro;
	}
	public void setClasseParceiro(String classeParceiro) {
		this.classeParceiro = classeParceiro;
	}
	public String getObservacoes() {
		return observacoes;
	}
	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}
	public Date getDataCadastro() {
		return dataCadastro;
	}
	public void setDataCadastro(Date dataCadastro) {
		this.dataCadastro = dataCadastro;
	}
	public DateTimeSyntax getDataAlteracao() {
		return dataAlteracao;
	}
	public void setDataAlteracao(DateTimeSyntax dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getTelefone() {
		return telefone;
	}
	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}
	public BigDecimal getCodigoEndereco() {
		return codigoEndereco;
	}
	public void setCodigoEndereco(BigDecimal codigoEndereco) {
		this.codigoEndereco = codigoEndereco;
	}
	public String getTipoEndereco() {
		return tipoEndereco;
	}
	public void setTipoEndereco(String tipoEndereco) {
		this.tipoEndereco = tipoEndereco;
	}
	public String getEndereco() {
		return endereco;
	}
	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}
	public String getNumeroEndereco() {
		return numeroEndereco;
	}
	public void setNumeroEndereco(String numeroEndereco) {
		this.numeroEndereco = numeroEndereco;
	}
	public String getComplemento() {
		return complemento;
	}
	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}
	public BigDecimal getCodigoBairro() {
		return codigoBairro;
	}
	public void setCodigoBairro(BigDecimal codigoBairro) {
		this.codigoBairro = codigoBairro;
	}
	public String getBairro() {
		return bairro;
	}
	public void setBairro(String bairro) {
		this.bairro = bairro;
	}
	public BigDecimal getCodigoCidade() {
		return codigoCidade;
	}
	public void setCodigoCidade(BigDecimal codigoCidade) {
		this.codigoCidade = codigoCidade;
	}
	public String getCidade() {
		return cidade;
	}
	public void setCidade(String cidade) {
		this.cidade = cidade;
	}
	public String getUf() {
		return uf;
	}
	public void setUf(String uf) {
		this.uf = uf;
	}
	public String getCep() {
		return cep;
	}
	public void setCep(String cep) {
		this.cep = cep;
	}
	public String getPontoReferencia() {
		return pontoReferencia;
	}
	public void setPontoReferencia(String pontoReferencia) {
		this.pontoReferencia = pontoReferencia;
	}
	public BigDecimal getCodigoPerfil() {
		return codigoPerfil;
	}
	public void setCodigoPerfil(BigDecimal codigoPerfil) {
		this.codigoPerfil = codigoPerfil;
	}
	public String getPerfil() {
		return perfil;
	}
	public void setPerfil(String perfil) {
		this.perfil = perfil;
	}
	public String getRetemIss() {
		return retemIss;
	}
	public void setRetemIss(String retemIss) {
		this.retemIss = retemIss;
	}
	public String getCadastroLiberado() {
		return cadastroLiberado;
	}
	public void setCadastroLiberado(String cadastroLiberado) {
		this.cadastroLiberado = cadastroLiberado;
	}
	
	public String getJsonUpdatePartner(DynamicVO parceiroVO) {

		Gson gson                  = new Gson();
		String json                = "";
		this.codigoParceiroSankhya = parceiroVO.asBigDecimal("CODPARC");
		this.codigoIntegracao      = parceiroVO.asBigDecimal("AD_CODPARCEXT");
		this.ativo                 = parceiroVO.asString("ATIVO");
		this.cadastroLiberado      = parceiroVO.asString("AD_LIBERADO");
		this.codigoCidade          = parceiroVO.asBigDecimal("CODCID");
		//this.codigoBairro        = parceiroVO.asBigDecimal("CODBAI");     // Nao permite pesquisa
		//this.codigoEndereco      = parceiroVO.asBigDecimal("CODEND");     // Nao permite pesquisa
		//this.codigoPerfil        = parceiroVO.asBigDecimal("CODTIPPARC"); // Nao permite pesquisa
		//this.limiteCredito       = parceiroVO.asBigDecimal("LIMCRED");    // Nao permite pesquisa

		if(parceiroVO.asString("CGC_CPF") != null && !parceiroVO.asString("CGC_CPF").isEmpty())               { this.cpf                   = parceiroVO.asString("CGC_CPF"); }
		if(parceiroVO.asString("EMAIL") != null && !parceiroVO.asString("EMAIL").isEmpty())                   { this.email                 = parceiroVO.asString("EMAIL"); }
		if(parceiroVO.asString("HOMEPAGE") != null && !parceiroVO.asString("HOMEPAGE").isEmpty())             { this.site                  = parceiroVO.asString("HOMEPAGE"); }
		if(parceiroVO.asString("IDENTINSCESTAD") != null && !parceiroVO.asString("IDENTINSCESTAD").isEmpty()) { this.ie                    = parceiroVO.asString("IDENTINSCESTAD"); }
		if(parceiroVO.asString("NOMEPARC") != null && !parceiroVO.asString("NOMEPARC").isEmpty())             { this.nomeParceiro          = parceiroVO.asString("NOMEPARC"); }
		if(parceiroVO.asString("NUMEND") != null && !parceiroVO.asString("NUMEND").isEmpty())                 { this.numeroEndereco        = parceiroVO.asString("NUMEND"); }
		if(parceiroVO.asString("RAZAOSOCIAL") != null && !parceiroVO.asString("RAZAOSOCIAL").isEmpty())       { this.razaoSocial           = parceiroVO.asString("RAZAOSOCIAL"); }
		if(parceiroVO.asString("TELEFONE") != null && !parceiroVO.asString("TELEFONE").isEmpty())             { this.telefone              = parceiroVO.asString("TELEFONE"); }
		//if(!parceiroVO.asString("AD_CLASSE_PARCEIRO").isEmpty())  { this.classeParceiro        = parceiroVO.asString("AD_CLASSE_PARCEIRO"); } // Nao permite pesquisa
		//if(!parceiroVO.asString("AD_LIBERADO").isEmpty())         { this.cadastroLiberado      = parceiroVO.asString("AD_LIBERADO"); }        // Nao permite pesquisa
		//if(!parceiroVO.asString("AD_PONTOREFERENCIA").isEmpty())  { this.pontoReferencia       = parceiroVO.asString("AD_PONTOREFERENCIA"); } // Nao permite pesquisa
		//if(!parceiroVO.asString("ATIVO").isEmpty())               { this.ativo                 = parceiroVO.asString("ATIVO"); }              // Nao permite pesquisa
		//if(!parceiroVO.asString("CEP").isEmpty())                 { this.cep                   = parceiroVO.asString("CEP"); }                // Nao permite pesquisa
		//if(!parceiroVO.asString("CLASSIFICMS").isEmpty())         { this.classificacaoICMS     = parceiroVO.asString("CLASSIFICMS"); }        // Nao permite pesquisa
		//if(!parceiroVO.asString("CLIENTE").isEmpty())             { this.cliente               = parceiroVO.asString("CLIENTE"); }            // Nao permite pesquisa
		//if(!parceiroVO.asString("COMPLEMENTO").isEmpty())         { this.complemento           = parceiroVO.asString("COMPLEMENTO"); }        // Nao permite pesquisa
		//if(!parceiroVO.asString("DTULTCONTATO").isEmpty())        { this.dataUltimoContato     = parceiroVO.asString("DTULTCONTATO"); }       // Nao permite pesquisa
		//if(!parceiroVO.asString("FORNECEDOR").isEmpty())          { this.fornecedor            = parceiroVO.asString("FORNECEDOR"); }         // Nao permite pesquisa
		//if(!parceiroVO.asString("INSCMUN").isEmpty())             { this.im                    = parceiroVO.asString("INSCMUN"); }            // Nao permite pesquisa
		//if(!parceiroVO.asString("OBSERVACOES").isEmpty())         { this.observacoes           = parceiroVO.asString("OBSERVACOES"); }        // Nao permite pesquisa
		//if(!parceiroVO.asString("PERFILECONECT").isEmpty())       { this.perfil                = parceiroVO.asString("PERFILECONECT"); }      // Nao permite pesquisa
		//if(!parceiroVO.asString("RETEMISS").isEmpty())            { this.retemIss              = parceiroVO.asString("RETEMISS"); }           // Nao permite pesquisa
		//if(!parceiroVO.asString("TIPPESSOA").isEmpty())           { this.tipoPessoa            = parceiroVO.asString("TIPPESSOA"); }          // Nao permite pesquisa
		//if(!parceiroVO.asString("TRANSPORTADORA").isEmpty())      { this.transportadora        = parceiroVO.asString("TRANSPORTADORA"); }     // Nao permite pesquisa
		//if(!parceiroVO.asString("USUARIO").isEmpty())             { this.usuario               = parceiroVO.asString("USUARIO"); }            // Nao permite pesquisa

		json = gson.toJson(this);		
		return json;
	}
}
