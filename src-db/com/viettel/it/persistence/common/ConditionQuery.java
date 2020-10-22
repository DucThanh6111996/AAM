package com.viettel.it.persistence.common;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.LikeExpression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.NotNullExpression;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.internal.CriteriaImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConditionQuery {

    
    private List<Criterion> criterions = new ArrayList<Criterion>();
    
    public void add(Criterion criterion) {
        criterions.add(criterion);
    }
    
    public void build(Criteria criteria) {
    	
        for(Criterion criterion : criterions) {
			createAlias(criterion,criteria);
            criteria.add(criterion);
        }
    }
    private void createAlias(Criterion criterion, Criteria criteria){
    	if (criterion instanceof SimpleExpression) {
			String fieldName = ((SimpleExpression) criterion).getPropertyName();
			createAlias(criteria,fieldName);
		}else if (criterion instanceof LikeExpression) {
			//String fieldName = ((LikeExpression) criterion).get;
		}else if (criterion instanceof LogicalExpression) {
			//String[] as = ((LogicalExpression)criterion).toString().split(((LogicalExpression)criterion).getOp(), -1);
			
		}else if (criterion instanceof Disjunction){
			for (Iterator<?> iterator = ((Disjunction)criterion).conditions().iterator(); iterator.hasNext();) {
				createAlias((Criterion) iterator.next(), criteria);
			}
		}else if(criterion instanceof NotNullExpression){
			String f = ((NotNullExpression)criterion).toString().replace(" is not null", "");
			createAlias(criteria,f);
		}
    }

	private void createAlias(Criteria criteria, String fieldName) {
		String[] prs = fieldName.split("\\.", -1);
		for (int i = 0; i < prs.length - 1; i++) {
			String alias = "";
			String aliasNew = "";
			for (int j = 0; j <= i; j++) {
				alias += prs[j] + ".";
				aliasNew += prs[j] + (j == i - 2 ? "." : "");
			}
			alias = alias.replaceAll("\\.$", "");
			Iterator<CriteriaImpl.Subcriteria> iter = ((CriteriaImpl) criteria).iterateSubcriteria();

			if (iter.hasNext()) {
				int c = 0;
				while (iter.hasNext()) {
					Criteria subcriteria = iter.next();
					if (subcriteria.getAlias() != null && subcriteria.getAlias().contains(aliasNew)) {
						c++;
						break;
					}
				}
				if (c == 0) {
					criteria.createAlias(alias, aliasNew);

				}
			} else {
				criteria.createAlias(alias, aliasNew);

			}
			if (i < prs.length - 2)
				fieldName = fieldName.replaceFirst("\\.", "");
		}
	}
        
}
