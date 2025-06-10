 package at.codemaestro.domain.organization;

 import at.codemaestro.domain.account.Account;
 import jakarta.persistence.Column;
 import jakarta.persistence.Embeddable;
 import jakarta.persistence.EmbeddedId;
 import jakarta.persistence.Entity;
 import jakarta.persistence.EnumType;
 import jakarta.persistence.Enumerated;
 import jakarta.persistence.FetchType;
 import jakarta.persistence.Id;
 import jakarta.persistence.ManyToOne;
 import jakarta.persistence.MapsId;
 import jakarta.validation.Valid;
 import jakarta.validation.constraints.NotNull;
 import lombok.AllArgsConstructor;
 import lombok.Builder;
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 import lombok.ToString;

 @Entity
 @Getter
 @NoArgsConstructor
 public class OrganizationAccountRelation {

     @Id
     @EmbeddedId
     @NotNull
     @Valid
     private OrganizationAccountRelation.OrganizationAccountRelationId id;

     @ManyToOne(optional = false)
     @MapsId("accountId")
     private Account account;

     @ManyToOne(optional = false, fetch = FetchType.LAZY)
     @MapsId("organizationId")
     private Organization organization;

     @Column(nullable = false)
     @Enumerated(EnumType.STRING)
     @NotNull @Setter
     private OrganizationRole role;

     @Embeddable
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     @ToString
     @EqualsAndHashCode
     @Getter
     public static class OrganizationAccountRelationId {
         @NotNull
         private String accountId;

         @NotNull
         private Long organizationId;
     }

     public Long getOrganizationId() {
         return this.id.getOrganizationId();
     }

     public String getUsername() {
         return this.id.accountId;
     }

     @Builder
     public OrganizationAccountRelation(Account account, Organization organization, OrganizationRole role) {
         this.id = new OrganizationAccountRelationId(account.getUsername(), organization.getId());
         this.account = account;
         this.organization = organization;
         this.role = role;
     }
 }
