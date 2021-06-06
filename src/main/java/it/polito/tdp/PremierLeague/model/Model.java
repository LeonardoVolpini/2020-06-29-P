package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private SimpleWeightedGraph<Match, DefaultWeightedEdge> grafo;
	private PremierLeagueDAO dao;
	private Map<Integer,Match> idMap;
	private List<Adiacenza> adiacenze;
	private boolean grafoCreato;
	
	private List<Match> percorsoBest;
	private Integer pesoMax;
	
	public Model() {
		this.dao = new PremierLeagueDAO();
		this.idMap= new HashMap<>();
		this.dao.listAllMatches(idMap);
		this.adiacenze= new ArrayList<>();
		this.grafoCreato=false;
		this.percorsoBest= new ArrayList<>();
	}
	
	public void creaGrafo(int min, int mese) {
		this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(grafo, dao.getVertici(idMap,mese));
		this.adiacenze= dao.getAdiacenze(idMap, min, mese);
		
		for (Adiacenza a : this.adiacenze) {
			if (grafo.vertexSet().contains(a.getM1()) && grafo.vertexSet().contains(a.getM2())) {
				Graphs.addEdgeWithVertices(grafo, a.getM1(), a.getM2(), (double)a.getPeso());
			}
		}
		this.grafoCreato=true;
	}
	
	public boolean isGrafoCreato() {
		return grafoCreato;
	}

	public int getNumVertici() {
		return grafo.vertexSet().size();
	}
	
	public int getNumArchi() {
		return grafo.edgeSet().size();
	}
	
	public List<Adiacenza> ConessioneMax() {
		int max=0;
		List<Adiacenza> best= new ArrayList<>();
		for (Adiacenza a : this.adiacenze) {
			if (a.getPeso()>max) {
				max=a.getPeso();
			}
		}
		for (Adiacenza a : this.adiacenze) {
			if (a.getPeso()==max)
				best.add(a);
		}
		if (best.isEmpty()) {
			return null;
		}
		return best;
	}
	
	public Set<Match> getVertici(){
		return grafo.vertexSet();
	}
	
	public List<Match> percorsoMax(Match partenza, Match arrivo){
		this.percorsoBest=null;
		this.pesoMax=0;
		List<Match> parziale = new ArrayList<>();
		parziale.add(partenza);
		ricorsione(parziale,arrivo,pesoMax);
		return this.percorsoBest;
	}
	
	private void ricorsione(List<Match> parziale, Match arrivo, int pesoMax) {
		Match ultimo = parziale.get(parziale.size()-1);
		if (ultimo.equals(arrivo)) { //caso terminale
			int peso= pesoParziale(parziale);
			if (this.percorsoBest==null  || peso>pesoMax) {
				this.pesoMax=peso;
				this.percorsoBest= new ArrayList<>(parziale);
				return;
			}
			else
				return; // sono arrivato al Match di arrivo ma il percorso ottenuto non è il più lungo
		}
		
		//faccio ricorsione:
		for (DefaultWeightedEdge e : grafo.edgesOf(ultimo)) { //devo controllare di evitare i cicli
			Match prossimo = Graphs.getOppositeVertex(grafo, e, ultimo);
			//controllo che non ho il match successivo con le stesse squadre:
			if ( (prossimo.getTeamHomeID().equals(ultimo.getTeamHomeID()) && prossimo.getTeamAwayID().equals(ultimo.getTeamAwayID()) ) ||
				  ( prossimo.getTeamHomeID().equals(ultimo.getTeamAwayID()) && prossimo.getTeamAwayID().equals(ultimo.getTeamHomeID())  )	   )
				continue; //salto l'iterazione ma continuo il ciclo
			if (!parziale.contains(prossimo)) {
				parziale.add(prossimo);
				ricorsione(parziale,arrivo,pesoMax);
				parziale.remove(parziale.size()-1);
			}
		}
	}
	
	private Integer pesoParziale(List<Match> parziale) {
		int peso=0;
		int i=0; //indice che mi serve per prendere il match successivo in parziale
		for (Match m : parziale) {
			if (i==(parziale.size()-1)) 
				break;
			DefaultWeightedEdge e = grafo.getEdge(m, parziale.get(i+1));
			i++;
			peso += grafo.getEdgeWeight(e);
		}
		return peso;
	}
	
	public int getPesoMax() {
		return this.pesoMax;
	}
}
