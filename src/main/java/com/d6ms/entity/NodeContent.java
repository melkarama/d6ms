package com.d6ms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "DMS_NODE_CONTENT")
public class NodeContent extends BaseEntity {

	@Lob
	@Column(name = "CONTENT")
	private byte[] content;

}
