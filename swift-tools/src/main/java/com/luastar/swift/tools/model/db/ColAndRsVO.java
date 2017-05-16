package com.luastar.swift.tools.model.db;

import com.luastar.swift.tools.model.PropBaseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ColAndRsVO extends PropBaseVO {

	private static final Logger logger = LoggerFactory.getLogger(ColAndRsVO.class);

	protected List<ColumnVO> colList;
	protected List<Object[]> valueList;

	public List<ColumnVO> getColList() {
		return colList;
	}

	public void setColList(List<ColumnVO> colList) {
		this.colList = colList;
	}

	public List<Object[]> getValueList() {
		return valueList;
	}

	public void setValueList(List<Object[]> valueList) {
		this.valueList = valueList;
	}

	private boolean checkVO() {
		if (colList == null || colList.isEmpty() || valueList == null || valueList.isEmpty()
				|| colList.size() != valueList.get(0).length) {
			logger.info("ColAndRsVO数据为空!");
			return false;
		}
		return true;
	}

	public int getRowCount() {
		if (checkVO()) {
			return valueList.size();
		}
		return 0;
	}

	/**
	 * 替换值
	 * @param keys 需要替换的字段
	 * @param values 替换的值[行索引][替换字段索引]
	 */
	public void replaceValue(String[] keys, Object[][] values) {
		if (!checkVO()) {
			throw new RuntimeException("ColAndRsVO数据为空!");
		}
		if (keys == null || keys.length == 0 || values == null || values.length == 0
				|| valueList.size() != values.length || keys.length != values[0].length) {
			throw new RuntimeException("设置替换值参数不匹配!");
		}
		for (int i = 0, il = valueList.size(); i < il; i++) {
			for (int j = 0; j < keys.length; j++) {
				int keyIndex = -1;
				lableA: for (int k = 0, kl = colList.size(); k < kl; k++) {
					if (keys[j].equalsIgnoreCase(colList.get(k).getColumnName())) {
						keyIndex = k;
						break lableA;
					}
				}
				if (keyIndex != -1) {
					valueList.get(i)[keyIndex] = values[i][j];
				}
			}
		}
	}

	public Object getColValue(int row, String key) {
		if (row < 0 || row >= valueList.size()) {
			return null;
		}
		int keyIndex = -1;
		for (int j = 0, jl = colList.size(); j < jl; j++) {
			if (key.equalsIgnoreCase(colList.get(j).getColumnName())) {
				keyIndex = j;
			}
		}
		if (keyIndex != -1) {
			return valueList.get(row)[keyIndex];
		}
		return null;
	}

	public String getColString() {
		StringBuffer sb = new StringBuffer();
		sb.append(colList.get(0).getColumnName());
		for (int i = 1, l = colList.size(); i < l; i++) {
			sb.append(",").append(colList.get(i).getColumnName());
		}
		return sb.toString();
	}

	public String[] getValueString() {
		String[] rs = new String[valueList.size()];
		for (int i = 0, il = valueList.size(); i < il; i++) {
			StringBuffer sb = new StringBuffer();
			for (int j = 0, jl = colList.size(); j < jl; j++) {
				String colName = colList.get(j).getColumnName();
				Object value = valueList.get(i)[j];
				if (value == null) {
					sb.append(j == 0 ? "" : ",").append("null");
				} else {
					String colType = colList.get(j).getColumnType();
					if (colType.toLowerCase().contains("char")) {
						sb.append(j == 0 ? "'" : ",'").append(value).append("'");
					} else {
						sb.append(j == 0 ? "" : ",").append(value);
					}
				}
			}
			rs[i] = sb.toString();
		}
		return rs;
	}

}
