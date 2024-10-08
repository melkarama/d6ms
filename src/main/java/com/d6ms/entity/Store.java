package com.d6ms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "DMS_STORE")
public class Store extends BaseEntity {

	@Column(name = "NAME")
	private String name;

	public Store(String id) {
		super(id);
	}

	public Store() {
	}

}
