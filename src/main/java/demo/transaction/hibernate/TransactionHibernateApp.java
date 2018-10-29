package demo.transaction.hibernate;

/*-
 * #%L
 * actframework app demo - Transaction (hibernate)
 * %%
 * Copyright (C) 2018 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static act.controller.Controller.Util.redirect;
import static act.controller.Controller.Util.render;

import act.Act;
import act.app.ActionContext;
import act.db.jpa.JPAContext;
import act.db.sql.tx.Transactional;
import act.job.OnAppStart;
import org.osgl.$;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;
import org.osgl.mvc.result.Result;
import org.osgl.util.IntRange;

import javax.inject.Inject;

/**
 * A Simple Todo application controller
 */
public class TransactionHibernateApp {

    public static final String ACC_A = "A";
    public static final String ACC_B = "B";

    @Inject
    private Account.Dao dao;

    @GetAction
    public Result home() {
        Account accA = dao.findById(ACC_A);
        Account accB = dao.findById(ACC_B);
        return render(accA, accB);
    }

    @PostAction("/transfer")
    public Result transfer(int amount, boolean btnA2B, boolean btnB2A, ActionContext context) {
        boolean success;
        if (btnA2B) {
            success = dao.transfer(amount, ACC_A, ACC_B);
        } else {
            success = dao.transfer(amount, ACC_B, ACC_A);
        }
        if (!success) {
            context.flash().error("Transaction failed. Possible reason: no enough balance in the credit account");
        } else {
            context.flash().success("Transaction committed successfully");
        }
        return redirect("/");
    }

    @OnAppStart
    public void ensureTestingData() {
        JPAContext.init();
        try {
            loadTestingData();
        } finally {
            JPAContext.close();
        }
    }

    @Transactional
    private void loadTestingData() {
        Account a = dao.findById(TransactionHibernateApp.ACC_A);
        if (null == a) {
            a = new Account(TransactionHibernateApp.ACC_A);
            a.setAmount($.random(IntRange.of(100, 2000)));
            dao.save(a);
        }
        Account b = dao.findById(TransactionHibernateApp.ACC_B);
        if (null == b) {
            b = new Account(TransactionHibernateApp.ACC_B);
            b.setAmount($.random(IntRange.of(200, 300)));
            dao.save(b);
        }
    }


    public static void main(String[] args) throws Exception {
        Act.start("Transaction Demo");
    }


}
