package controllers;


import models.Envelope;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.avaje.ebean.Ebean;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class DocusignConnect extends Controller{
	
	public static Result recieveXml() {
		org.w3c.dom.Document dom = request().body().asXml();
		
		NodeList nlist = dom.getElementsByTagName("EnvelopeID");
		Node nNode = nlist.item(0);
		
		Envelope env = Ebean.find(Envelope.class).where()  
			      .eq("envelope_id", nNode.getTextContent()).findUnique();
		
		//Update necessary details and upload signed document(if signed) to smartsheet
		if(null != env){
			nlist = dom.getElementsByTagName("Status");
			Logger.debug(nlist.getLength()+"");
			for(int i=0; i < nlist.getLength(); i++){
				Logger.debug(nlist.item(i).getTextContent());
				if(nlist.item(i).getTextContent().equals("signed")){
					env.setStatus(env.getStatus().SIGNED);
				}
			}
		}
		Ebean.save(env);
		return ok();
	}
}
