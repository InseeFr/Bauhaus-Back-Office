package fr.insee.rmes.model.links;

import fr.opensagres.xdocreport.core.utils.StringUtils;

import java.util.Objects;

public class OperationsLink {

	public static final String CLASS_NAME = "fr.insee.rmes.model.links.OperationsLink";

	public String id;
	
	public String type;

	public String labelLg1;

	public String labelLg2;

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getLabelLg1() {
		return labelLg1;
	}

	public String getLabelLg2() {
		return labelLg2;
	}

	public OperationsLink(String id, String type, String labelLg1, String labelLg2) {
		super();
		this.id = id;
		this.type = type;
		this.labelLg1 = labelLg1;
		this.labelLg2 = labelLg2;
	}
	
	public OperationsLink() {
		super();
	}

	public boolean isEmpty() {
		return StringUtils.isEmpty(id);
	}
	
	public static String getClassOperationsLink() {
		return CLASS_NAME;
	}


	@Override
	public int hashCode() {
		return Objects.hash(id, labelLg1, labelLg2, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OperationsLink other = (OperationsLink) obj;
		return Objects.equals(id, other.id) && Objects.equals(labelLg1, other.labelLg1)
				&& Objects.equals(labelLg2, other.labelLg2) && Objects.equals(type, other.type);
	}
}
