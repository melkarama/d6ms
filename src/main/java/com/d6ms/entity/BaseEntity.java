package com.d6ms.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.d6ms.type.State;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@MappedSuperclass
@EqualsAndHashCode(of = "id")
public class BaseEntity<ID extends Serializable> {

	@Id
	@Column(name = "ID")
	private ID id;

	@ManyToOne
	@JoinColumns(@JoinColumn(name = "STORE_ID", nullable = false))
	private Store store;

	@Column(name = "CREATION_DATE")
	private LocalDateTime creationDate;

	@Column(name = "UPDATE_DATE")
	private LocalDateTime updateDate;

	@Column(name = "ARCHIVE_DATE")
	private LocalDateTime archiveDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATE")
	private State state;

	@PrePersist
	protected void onCreate() {
		this.creationDate = LocalDateTime.now();
		this.updateDate = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.updateDate = LocalDateTime.now();

		if (state == State.ARCHIVED && archiveDate == null) {
			this.archiveDate = LocalDateTime.now();
		}
	}

}
