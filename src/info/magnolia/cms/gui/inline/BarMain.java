/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */



package info.magnolia.cms.gui.inline;

import info.magnolia.cms.util.Resource;
import info.magnolia.cms.gui.control.Bar;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.beans.config.Server;

import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;
import javax.jcr.access.Permission;
import java.util.ArrayList;
import java.io.IOException;

/**
 *
 * User: enz
 * Date: Jul 19, 2004
 * Time: 5:05:22 PM
 *
 */
public class BarMain extends Bar {
	private Button buttonEditView=new Button();
	private Button buttonPreview=new ButtonEdit();
	private Button buttonProperties=new Button();
	private Button buttonSiteAdmin=new Button();
	private int top=0;
	private int left=0;
	private String width="100%";
	boolean overlay=true;


	public BarMain(HttpServletRequest request) {
		this.setRequest(request);
	}

	public BarMain(HttpServletRequest request, String path,String nodeCollectionName, String nodeName, String paragraph) {
		this.setRequest(request);
		this.setPath(path);
		this.setNodeCollectionName(nodeCollectionName);
		this.setNodeName(nodeName);
		this.setParagraph(paragraph);
	}


	/**
	* <p>sets the default buttons</p>
	*
	* */
	public void setDefaultButtons() {
		this.setButtonEditView();
		this.setButtonPreview();
		this.setButtonSiteAdmin();
		this.setButtonProperties();
	}

	/**
	* <p>places the default buttons to the very right/left position</p>
	*
	* */
	public void placeDefaultButtons() {
		this.getButtonsLeft().add(0,this.getButtonSiteAdmin());
		this.getButtonsLeft().add(0,this.getButtonPreview());
		this.getButtonsRight().add(this.getButtonsRight().size(),this.getButtonProperties());
	}


	public Button getButtonProperties() { return this.buttonProperties;}
	public void setButtonProperties(Button b) {this.buttonProperties=b;}
	public void setButtonProperties() {
		this.setButtonProperties(this.getPath(),this.getParagraph());
	}
	/**
	* <p>sets the default page properties button</p>
	* @param path , path of the current page
	 * @param paragraph , paragraph type
	* */
	public void setButtonProperties(String path,String paragraph) {
		ButtonEdit b=new ButtonEdit();
		b.setLabel("Properties");
		b.setPath(path);
		b.setParagraph(paragraph);
		b.setDefaultOnclick();
		this.setButtonProperties(b);
	}

	public Button getButtonPreview() { return this.buttonPreview;}
	public void setButtonPreview(Button b) {this.buttonPreview=b;}
	/**
	* <p>sets the default preview button (to switch from edit to preview mode)</p>
	* */
	public void setButtonPreview() {
		Button b=new Button();
		b.setLabel("&laquo; Preview");
		b.setOnclick("mgnlPreview(true);");
		this.setButtonPreview(b);
	}


	public Button getButtonEditView() { return this.buttonEditView;}
	public void setButtonEditView(Button b) {this.buttonEditView=b;}
	/**
	* <p>sets the default edit view button (to switch form preview to edit view mode)</p>
	* */
	public void setButtonEditView() {
		Button b=new Button();
		b.setLabel("&raquo;");
		b.setLabelNbspPadding(1);
		b.setOnclick("mgnlPreview(false);");
		this.setButtonEditView(b);
	}


	public Button getButtonSiteAdmin() { return this.buttonSiteAdmin;}
	public void setButtonSiteAdmin(Button b) {this.buttonSiteAdmin=b;}
	public void setButtonSiteAdmin() { this.setButtonSiteAdmin(this.getPath());}
	/**
	* <p>sets the default site admin button</p>
	 * @param path , path of the current page (will show up in site admin)
	* */
	public void setButtonSiteAdmin(String path) {
		Button b=new Button();
		b.setLabel("adminCentral");
		b.setOnclick("mgnlOpenAdminCentral('"+path+"');");
		this.setButtonSiteAdmin(b);
	}


	public void setTop(int i) {this.top=i;}
	public int getTop() {return this.top;}

	public void setLeft(int i) {this.left=i;}
	public int getLeft() {return this.left;}

	public void setWidth(String s) {this.width=s;}
	public String getWidth() {return this.width;}


	/**
	* <p>sets if the main bar overlays the content (true, default) or if it is moving it downward (false)</p>
	* */
	public void setOverlay(boolean b) {this.overlay=b;}
	public boolean getOverlay() {return this.overlay;}

	/**
	* <p>draws the main bar (incl. all magnolia specific js and css links)</p>
	* */
	public void drawHtml(JspWriter out) throws IOException {
		if (Server.isAdmin()) {
			this.drawHtmlLinks(out);

			int top=this.getTop();
			int left=this.getLeft();

			//todo: attribute for preview name not static!
			//todo: a method to get preview?
			String prev=(String) this.getRequest().getSession().getAttribute("mgnlPreview");
			boolean isGranted=Resource.getActivePage(this.getRequest()).isGranted(Permission.SET_PROPERTY);
			if (isGranted) {
				if (prev==null) {
					//is edit mode
					this.setSmall(false);

					if (this.getOverlay()) out.println("<div style=\"position:absolute;top:"+top+"px;left:"+left+"px;width:"+this.getWidth()+";z-index:900;\">");
					out.println(this.getHtml());
					if (this.getOverlay()) out.println("</div>");
				}
				else {
					//is in preview mode
					top+=4;
					left+=4;
					out.println("<div style=\"position:absolute;top:"+top+"px;left:"+left+"px;z-index:900;\">");
					out.println("&nbsp;");
					out.println(this.getButtonEditView().getHtml());
					out.println("</div>");
				}
			}
		}
	}

	/**
	* <p>draws the  magnolia specific js and css links)</p>
	* */
	public void drawHtmlLinks(JspWriter out) throws IOException {
		//@todo: src/href not static - move
		out.println("<script src='/admindocroot/js/general.js'></script>");
		out.println("<script src='/admindocroot/js/generic.js'></script>");
		out.println("<script src='/admindocroot/js/controls.js'></script>");
		out.println("<script src='/admindocroot/js/inline.js'></script>");
		out.println("<script src='/admindocroot/js/dialogs/dialogs.js'></script>");
		out.println("<script src='/admindocroot/js/tree.js'></script>");
		out.println("<script src='/admindocroot/js/adminCentral.js'></script>");
		out.println("<link rel='stylesheet' type='text/css' href='/admindocroot/css/controls.css'/>");
	}


}
