package it.schmid.android.mofa.model;

import java.util.Date;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
@DatabaseTable
public class Work {
	public final static String ID_FIELD_NAME = "id";
	@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
	@Expose
	private Integer id;
	@DatabaseField(dataType = DataType.DATE_LONG)
	@Expose
	private Date date;
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	@Expose
	private Task task;
	@DatabaseField
	@Expose
	private String note;
	@DatabaseField(index = true, defaultValue = "0")
	@Expose
	private Boolean valid;
	@DatabaseField(index = true, defaultValue = "0")
	@Expose
	private Boolean sended;
	@DatabaseField
	private String data;


	//constructor
	public Work() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Boolean getSended() {
		return sended;
	}

	public void setSended(Boolean sended) {
		this.sended = sended;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}

