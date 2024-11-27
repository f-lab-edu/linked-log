package flab.Linkedlog.repository;

import flab.Linkedlog.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;


    public void save (Member member) {
        em.persist(member);
    }

    public Member findById(UUID id) {
        return em.find(Member.class, id);
    }

    public Member findByUserId(String userId) {
        List<Member> members = em.createQuery("select m from Member m where m.userId = :userId", Member.class)
                .setParameter("userId", userId)
                .getResultList();

        if (members.isEmpty()) {
            return null;
        } else if (members.size() == 1) {
            return members.get(0);
        } else {
            throw new IllegalStateException("There are more than one member with userId = " + userId);
        }
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public void delete (Member member) {
        em.remove(member);
    }


}
