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



package info.magnolia.cms.gui.dialog;

import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;
import info.magnolia.cms.gui.control.Hidden;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.ButtonSet;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 *
 * User: enz
 * Date: May 13, 2004
 * Time: 3:35:04 PM
 *
 */
public class DialogDialog extends DialogSuper {
	private static Logger log = Logger.getLogger(DialogDialog.class);
	public String callbackJavascript="opener.document.location.reload();window.close();";
	public String action;
	public boolean doSave=true; //todo: leftover?

	public String DIALOGSIZE_NORMAL_WIDTH="800";
	public String DIALOGSIZE_NORMAL_HEIGHT="650";
	public String DIALOGSIZE_SLIM_WIDTH="500";
	public String DIALOGSIZE_SLIM_HEIGHT="600";



	public ArrayList javascriptSources=new ArrayList();

	public ArrayList cssSources=new ArrayList();

	public DialogDialog(Content configNode,Content websiteNode,HttpServletRequest request,PageContext pageContext) {
		super(configNode,websiteNode,request,pageContext);
	}

	public DialogDialog() {
	}

	public void setCallbackJavascript(String s) {this.callbackJavascript=s;}
	public String getCallbackJavascript() {return this.callbackJavascript;}

	public void setAction(String s) {this.action=s;}
	public String getAction() {
		if (this.action==null) return this.getRequest().getRequestURI();
		else return this.action;
	}


	public void setJavascriptSources(String s) {this.getJavascriptSources().add(s);}
	public ArrayList getJavascriptSources() {return this.javascriptSources;}
	public void drawJavascriptSources(JspWriter out) {
		Iterator it=this.getJavascriptSources().iterator();
		while (it.hasNext()) {
			try {
				out.println("<script type=\"text/javascript\" src=\""+it.next()+"\"></script>");
			}
			catch (IOException ioe) {log.error("");}
		}
	}

	public void setCssSources(String s) {this.getCssSources().add(s);}
	public ArrayList getCssSources() {return this.cssSources;}
	public void drawCssSources(JspWriter out) {
		Iterator it=this.getCssSources().iterator();
		while (it.hasNext()) {
			try {
				out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\""+it.next()+"\">");
			}
			catch (IOException ioe) {log.error("");}
		}
	}


	public DialogTab addTab() {
		return this.addTab("");
	}

	public DialogTab addTab(String label) {
		DialogTab tab=new DialogTab();
		tab.setLabel(label);
		this.getSubs().add(tab);
		return tab;
	}

	public DialogTab getTab(int i) {
		return (DialogTab) this.getSubs().get(i);
	}



	public void drawHtmlPreSubs(JspWriter out) {
		try {
			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
			out.println("<html>");
			out.println("<head>");
			out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>"); //kupu
			out.println("<script type=\"text/javascript\">");
			out.println("window.resizeTo("+this.getConfigValue("width",this.DIALOGSIZE_NORMAL_WIDTH)+","+this.getConfigValue("height",this.DIALOGSIZE_NORMAL_HEIGHT)+");");
			out.println("</script>");
			out.println("<title>"+this.getConfigValue("label","Magnolia Edit Dialog")+"</title>");
			//todo: js/css sources
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/admindocroot/css/controls.css\">");
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/admindocroot/css/dialogs.css\">");
			out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/admindocroot/css/general.css\">");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/js/generic.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/js/general.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/js/controls.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/js/dialogs/dialogs.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/js/dialogs/calendar.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/js/tree.js\"></script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/js/adminCentral.js\"></script>");
			out.println("<link href=\"/admindocroot/richE/kupustyles.css\" rel=\"stylesheet\" type=\"text/css\"/>");
			out.println("<link href=\"/admindocroot/richE/kupucustom.css\" rel=\"stylesheet\" type=\"text/css\"/>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/richE/sarissa.js\"> </script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupuhelpers.js\"> </script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupueditor.js\"> </script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupubasetools.js\"> </script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupuloggers.js\"> </script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupucontentfilters.js\"> </script>");
			out.println("<script type=\"text/javascript\" src=\"/admindocroot/richE/kupuinit.js\"> </script>");
			out.println("<script type=\"text/javascript\">");
			out.println("var mgnlRichEditors=new Array();"); //will be extended at each richEdit control
			out.println("var kupu = null;");
			out.println("var kupuui = null;");
			out.println("</script>");
			this.drawJavascriptSources(out);
			out.println("</head>");
			out.println("<body class=\"mgnlDialogBody\" onload=\"mgnlDialogInit();\" marginwidth=\"0\" topmargin=\"0\" marginheight=\"0\" leftmargin=\"0\">");
			out.println("<form action=\""+this.getAction()+"\" name=\"mgnlFormMain\" method=\"post\" enctype=\"multipart/form-data\">");

			out.println(new Hidden("mgnlRepository",this.getConfigValue("repository"),false).getHtml());
			out.println(new Hidden("mgnlPath",this.getConfigValue("path"),false).getHtml());
			out.println(new Hidden("mgnlNodeCollection",this.getConfigValue("nodeCollection"),false).getHtml());
			out.println(new Hidden("mgnlNode",this.getConfigValue("node"),false).getHtml());
			out.println(new Hidden("mgnlJsCallback",this.getCallbackJavascript(),false).getHtml());

			out.println(new Hidden("mgnlRichE",this.getConfigValue("richE"),false).getHtml());
			out.println(new Hidden("mgnlRichEPaste",this.getConfigValue("richEPaste"),false).getHtml());

			if (this.getConfigValue("paragraph").indexOf(",")==-1) {
				out.println(new Hidden("mgnlParagraph",this.getConfigValue("paragraph"),false).getHtml());

			} //else multiple paragraph selection -> radios for selection


			//TabSet stuff
			String id=this.getId();
			out.println("<script type=\"text/javascript\">");
			out.println("mgnlControlSets['"+id+"']=new Object();");
			out.println("mgnlControlSets['"+id+"'].items=new Array();");
			out.println("mgnlControlSets['"+id+"'].resize=true;");
			out.println("</script>");
			//end TabSet stuff


		}
		catch (IOException ioe) {log.error(ioe);}
	}

	public void drawHtmlPostSubs(JspWriter out) {
		try {

			//TabSet stuff
			String id=this.getId();
			out.println("<div class=\""+CSSCLASS_TABSETBUTTONBAR+"\">");
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr><td  class=\""+CSSCLASS_TABSETBUTTONBAR+"\">");

			if (this.getOptions().size()!=0) {
				ButtonSet control=new ButtonSet();
				((Button) this.getOptions().get(0)).setState(Button.BUTTONSTATE_PUSHED);
				control.setButtons(this.getOptions());
				control.setName(this.getId());
				control.setSaveInfo(false);
				control.setButtonType(ButtonSet.BUTTONTYPE_PUSHBUTTON);
				out.println(control.getHtml());
			}

			out.println("</td></tr></table></div>");
			out.println("<script type=\"text/javascript\">");
			out.println("mgnlDialogResizeTabs('"+id+"');");
			out.println("mgnlDialogShiftTab('"+id+"',false,0)");
			out.println("</script>");
			//end TabSet stuff




			out.println("<div class=\""+CSSCLASS_TABSETSAVEBAR+"\">");
			//out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
			//out.println("<tr><td style=\"text-align:right;\">");

			Button save=new Button();
			save.setOnclick(this.getConfigValue("saveOnclick","mgnlDialogFormSubmit();"));
			save.setLabel(this.getConfigValue("saveLabel","Save"));
			out.println(save.getHtml());

			Button cancel=new Button();
			cancel.setOnclick(this.getConfigValue("cancelOnclick","window.close();"));
			cancel.setLabel(this.getConfigValue("cancelLabel","Cancel"));
			out.println(cancel.getHtml());

			//out.println("</td></tr></table>");
			out.println("</div>");

			out.println("</form></body></html>");
		}
		catch (IOException ioe) {log.error(ioe);}
	}


}
