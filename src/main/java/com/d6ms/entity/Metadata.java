package com.d6ms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "DMS_METADATA")
public class Metadata extends Node {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns(@JoinColumn(name = "NODE_ID"))
	private Node node;

	@Column(name = "VALUE", length = 4000)
	private String value;

}
