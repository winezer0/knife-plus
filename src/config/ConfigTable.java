package config;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import burp.BurpExtender;


public class ConfigTable extends JTable
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public ConfigTable(ConfigTableModel ConfigTableModel)
	{
		super(ConfigTableModel);
		this.setColumnModel(columnModel);
		this.setFillsViewportHeight(true);//在table的空白区域显示右键菜单
		//https://stackoverflow.com/questions/8903040/right-click-mouselistener-on-whole-jtable-component
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.setBorder(new LineBorder(new Color(0, 0, 0)));

		addClickSort();
		registerListeners();
		//switchEnable();//no need 
		//table.setupTypeColumn()//can't set here, only can after table data loaded.
	}

	@Override
	public void changeSelection(int row, int col, boolean toggle, boolean extend)
	{
		super.changeSelection(row, col, toggle, extend);
	}

	public int[] getSelectedModelRows() {
		int[] rows = getSelectedRows();

		for (int i=0; i < rows.length; i++){
			rows[i] = convertRowIndexToModel(rows[i]);//转换为Model的索引，否则排序后索引不对应〿
		}
		Arrays.sort(rows);//升序
		return rows;
	}

	private void addClickSort() {
		TableRowSorter<ConfigTableModel> sorter = new TableRowSorter<ConfigTableModel>((ConfigTableModel) this.getModel());
		ConfigTable.this.setRowSorter(sorter);

		JTableHeader header = this.getTableHeader();
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					//ConfigTable.this.getRowSorter().getSortKeys().get(0).getColumn();
					sorter.getSortKeys().get(0).getColumn();
					////当Jtable中无数据时，jtable.getRowSorter()是null
				} catch (Exception e1) {
					e1.printStackTrace(new PrintWriter(BurpExtender.callbacks.getStderr(), true));//working?
				}
			}
		});
	}
	

	private void registerListeners(){
		final ConfigTable _this = this;
		this.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseReleased( MouseEvent e ){
			}

			@Override
			public void mousePressed(MouseEvent e) {
				//no need
			}

		});
	}


	public void setupTypeColumn() {
		//call this function must after table data loaded !!!!
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addItem(ConfigEntry.Action_Add_Or_Replace_Header);
		comboBox.addItem(ConfigEntry.Action_Append_To_header_value);
		comboBox.addItem(ConfigEntry.Action_Remove_From_Headers);
		comboBox.addItem(ConfigEntry.Config_Basic_Variable);
		comboBox.addItem(ConfigEntry.Config_Custom_Payload);
		comboBox.addItem(ConfigEntry.Config_Custom_Payload_Base64);
		comboBox.addItem(ConfigEntry.Config_Chunked_Variable);
		comboBox.addItem(ConfigEntry.Config_Proxy_Variable);
		TableColumnModel typeColumn = this.getColumnModel();
		typeColumn.getColumn(2).setCellEditor(new DefaultCellEditor(comboBox));

		JCheckBox jc1 = new JCheckBox();
		typeColumn.getColumn(3).setCellEditor(new DefaultCellEditor(jc1));
//		//Set up tool tips for the sport cells.
//		DefaultTableCellRenderer renderer =
//				new DefaultTableCellRenderer();
//		renderer.setToolTipText("Click for combo box");
//		typeColumn.setCellRenderer(renderer);
	}
}
