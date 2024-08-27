package com.d6ms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "DMS_NODE_CONTENT")
public class NodeContent extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns(@JoinColumn(name = "STORE_ID", nullable = false))
	private Store store;

	@Lob
	@Column(name = "CONTENT")
	private byte[] content;

}
