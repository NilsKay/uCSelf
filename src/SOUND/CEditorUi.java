package SOUND;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class CEditorUi extends JPanel{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 872436L;

	public CEditorUi(CEditorModel model) {
		this.setLayout(new BorderLayout());
		this.add("Center", doFormLayout(model)); 
		this.add("South", getTool(model)); 
	}
	private JPanel getTool(CEditorModel model) {
		JPanel p = new JPanel();
		p.add(model.bt[model.PLAY]);
		p.add(model.bt[model.SAVE]);
		p.add(model.bt[model.START]);
		p.add(model.bt[model.STOP]);
		return p;
	}
	
	private JComponent doFormLayout(CEditorModel model) {
    	FormLayout layout = new FormLayout(
    			"left:pref,3dlu,l:pref,5dlu,  l:pref, 3dlu, l:pref,5dlu,  l:pref, 3dlu, l:pref,5dlu, l:pref, 3dlu, l:pref,5dlu",
    			"p, 3dlu, " +
    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu, p,1dlu,p,10dlu," +
    			"p, 3dlu,"+		
    			"p,1dlu,p,5dlu,  p,1dlu,p,5dlu,  p,1dlu,p,5dlu");
    			
    	layout.setColumnGroups(new int[][]{{1, 5, 9}});
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setDefaultDialogBorder();
        // Obtain a reusable constraints object to place components in the grid.
        CellConstraints cc = new CellConstraints();
        int row = 1;
        //--------------- First Row
        builder.addSeparator(model.def.text[51],		cc.xyw (1,  row, 15));
        row += 2;
        //---
        builder.add(model.la[model.MINFREQ],			cc.xy (1,  row));
        builder.add(model.la[model.MAXFREQ],			cc.xy (5,  row));
        builder.add(model.la[model.SEED],				cc.xy (9,  row));
        row += 2;
        builder.add(model.ptfs[model.MINFREQ],			cc.xy (1,  row));
        builder.add(model.uni[model.MINFREQ],			cc.xy (3,  row));
        builder.add(model.ptfs[model.MAXFREQ],			cc.xy (5,  row));
        builder.add(model.uni[model.MAXFREQ],			cc.xy (7,  row));
        builder.add(model.ptfs[model.SEED],				cc.xy (9,  row));
        builder.add(model.uni[model.SEED],				cc.xy (11,  row));
        builder.add(model.cb[model.USENOTES],			cc.xy (13,  row));
        row += 2;
      //---
        builder.add(model.la[model.MINDUR],				cc.xy (1,  row));
        builder.add(model.la[model.MAXDUR],				cc.xy (5,  row));
        builder.add(model.cla[model.DURSTEPS],			cc.xy (9,  row));
        row += 2;
        builder.add(model.ptfs[model.MINDUR],			cc.xy (1,  row));
        builder.add(model.uni[model.MINDUR],			cc.xy (3,  row));
        builder.add(model.ptfs[model.MAXDUR],			cc.xy (5,  row));
        builder.add(model.uni[model.MAXDUR],			cc.xy (7,  row));
        builder.add(model.combo[model.DURSTEPS],		cc.xyw (9,  row, 3));
        builder.add(model.cb[model.PREFLOWERNOTES],		cc.xy (13,  row));
        row += 2;
      //---
        builder.add(model.cla[model.MINLOUD],			cc.xy (1,  row));
        builder.add(model.cla[model.MAXLOUD],			cc.xy (5,  row));
        row += 2;
        builder.add(model.combo[model.MINLOUD],			cc.xyw (1,  row, 2));
        builder.add(model.combo[model.MAXLOUD],			cc.xyw(5,  row, 2));
        builder.add(model.cb[model.DOSTEREO],			cc.xy (9,  row));
        builder.add(model.cb[model.DOAKKORD],			cc.xy (13,  row));
        row += 2;
        //---
        builder.add(model.cla[model.MINVOICE],			cc.xy (1,  row));
        builder.add(model.cla[model.MAXVOICE],			cc.xy (5,  row));
        builder.add(model.cla[model.GAMMA],				cc.xy (9,  row));
        row += 2;
        builder.add(model.combo[model.MINVOICE],		cc.xyw (1,  row, 2));
        builder.add(model.combo[model.MAXVOICE],		cc.xyw(5,  row, 2));
        builder.add(model.combo[model.GAMMA],			cc.xyw(9,  row, 2));
        builder.add(model.cb[model.DOSHADOW],			cc.xy (13,  row));
        row += 2;
        //---
        builder.add(model.la[model.DELAY],				cc.xy (1,  row));
        row += 2;
        builder.add(model.ptfs[model.DELAY],			cc.xy (1,  row));
        builder.add(model.uni[model.DELAY],				cc.xy (3,  row));
        builder.add(model.cb[model.USESELECTION],		cc.xy (13,  row));
        builder.add(model.bt[model.SELECT],				cc.xy (9,  row));
        row += 2;
        //---
        builder.addSeparator(model.def.text[50],		cc.xyw (1,  row, 15));
        row += 2;
        //---
        builder.add(model.la[model.POPULATION],			cc.xy (1,  row));
        builder.add(model.la[model.NUMBERFITTEST],		cc.xy (5,  row));
        builder.add(model.la[model.INTERVAL],			cc.xy (9,  row));
        builder.add(model.la[model.FOOTPRINT],			cc.xy (13,  row));
        row += 2;
        builder.add(model.ptfs[model.POPULATION],		cc.xy (1,  row));
        //builder.add(model.uni[model.POPULATION],		cc.xy (3,  row));
        builder.add(model.ptfs[model.NUMBERFITTEST],	cc.xy (5,  row));
       // builder.add(model.uni[model.NUMBERFITTEST],		cc.xy (7,  row));
        builder.add(model.ptfs[model.INTERVAL],			cc.xy (9,  row));
        builder.add(model.uni[model.INTERVAL],			cc.xy (11,  row));
        builder.add(model.ptfs[model.FOOTPRINT],		cc.xy (13,  row));
        //builder.add(model.uni[model.FOOTPRINT],			cc.xy (15,  row));
        row += 2;
        //---
        builder.add(model.la[model.RANDOMWEIGHT],		cc.xy (1,  row));
        builder.add(model.la[model.DEGRESSION],			cc.xy (5,  row));
        builder.add(model.la[model.ITERATIONEN],		cc.xy (9,  row));
        row += 2;
        builder.add(model.ptfs[model.RANDOMWEIGHT],		cc.xy (1,  row));
        builder.add(model.ptfs[model.DEGRESSION],		cc.xy (5,  row));
        builder.add(model.ptfs[model.ITERATIONEN],		cc.xy (9,  row));
        row += 2;
        builder.addSeparator("",		cc.xyw (1,  row, 15));
        return builder.getPanel();
	}
}