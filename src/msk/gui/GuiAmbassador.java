package msk.gui;

import hla.rti.RTIambassador;
import msk.template.TemplateAmbassador;

public class GuiAmbassador extends TemplateAmbassador {

    public GuiAmbassador(GuiFederate guiFederate){
        super(guiFederate);
    }
    @Override
    protected void log( String message )
    {
        System.out.println( "GuiFederateAmbassador: "+ message );
    }
}
