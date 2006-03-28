package info.magnolia.module.owfe.commands;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.module.owfe.MgnlConstants;
import openwfe.org.engine.workitem.WorkItem;
import org.apache.commons.chain.Context;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * This class to convert the parameters depending on the context.
 * A new context would need a new way to handle parameters but this way we avoid creating too many classes.
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class ParametersSetterHelper {

    public HashMap translateParam(MgnlCommand command, Context context) {
        String[] expected = command.getExpectedParameters();
        //String[] accepted = command.getAcceptedParameters();

        HashMap params = new HashMap();

        WorkItem workItem = (WorkItem) context.get(MgnlConstants.INFLOW_PARAM);
        if (workItem != null) {
            for (int i = 0; i < expected.length; i++)
                params.put(expected[i], workItem.getAttribute(expected[i]).toString());
        } else {
            HttpServletRequest request = (HttpServletRequest) context.get(MgnlConstants.P_REQUEST);
//            HashMap param = (HashMap) context.get(MgnlConstants.INTREE_PARAM);
//            for (int i = 0; i < expected.length; i++) {
//                params.put(expected[i], param.get(expected[i]));
//            }
            for (int i = 0; i < expected.length; i++) {
                params.put(expected[i], request.getParameter(expected[i]));
            }

        }
        context.put(MgnlCommand.PARAMS, params);
        return params;
    }

}
