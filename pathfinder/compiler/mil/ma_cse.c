/**
 * @file
 *
 * MIL Algebra common subexpression elimination.
 *
 * Copyright Notice:
 * -----------------
 *
 *  The contents of this file are subject to the MonetDB Public
 *  License Version 1.0 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://monetdb.cwi.nl/Legal/MonetDBLicense-1.0.html
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 *
 *  The Original Code is the ``Pathfinder'' system. The Initial
 *  Developer of the Original Code is the Database & Information
 *  Systems Group at the University of Konstanz, Germany. Portions
 *  created by U Konstanz are Copyright (C) 2000-2004 University
 *  of Konstanz. All Rights Reserved.
 *
 *  Contributors:
 *          Jens Teubner <jens.teubner@uni-konstanz.de>
 *
 * $Id$
 */

#include <assert.h>

#include "pathfinder.h"
#include "ma_cse.h"
#include "array.h"
#include "oops.h"

static PFarray_t *subexps = NULL;

static bool
subexp_eq (PFma_op_t *a, PFma_op_t *b)
{
    unsigned int i;

    /* shortcut for the trivial case */
    if (a == b)
        return true;

    if (a->kind != b->kind)
        return false;

    /* test all the children */
    for (i = 0; i < MILALGEBRA_MAXCHILD && a->child[i]; i++)
        if (a->child[i] != b->child[i])
            return false;

    /* test semantic content */
    switch (a->kind) {

        case ma_lit_oid:
            return a->sem.lit_val.val.o == b->sem.lit_val.val.o;
        case ma_lit_int:
            return a->sem.lit_val.val.i == b->sem.lit_val.val.i;
        case ma_lit_dbl:
            return a->sem.lit_val.val.d == b->sem.lit_val.val.d;
        case ma_lit_str:
            return a->sem.lit_val.val.s == b->sem.lit_val.val.s;
        case ma_lit_bit:
            return a->sem.lit_val.val.b == b->sem.lit_val.val.b;

        case ma_new:
            return (a->sem.new.htype == b->sem.new.htype)
                    && (a->sem.new.ttype == b->sem.new.ttype);

        case ma_insert:
        case ma_seqbase:
        case ma_project:
        case ma_reverse:
        case ma_sort:
        case ma_ctrefine:
        case ma_join:
        case ma_leftjoin:
        case ma_mirror:
        case ma_kunique:
        case ma_mark_grp:
        case ma_mark:
        case ma_count:
        case ma_append:
        case ma_oid:
        case ma_moid:
        case ma_mint:
        case ma_madd:
        case ma_serialize:
            return true;
    }

    PFoops (OOPS_FATAL, "Error in MIL Algebra CSE.");

}

static PFma_op_t *
ma_cse (PFma_op_t *n)
{
    unsigned int i;

    /* Do CSE for all the children */
    for (i = 0; i < MILALGEBRA_MAXCHILD && n->child[i]; i++)
        n->child[i] = ma_cse (n->child[i]);

    /* See if we already saw that subexpression */
    for (i = 0; i < PFarray_last (subexps); i++)
        if (subexp_eq (n, *((PFma_op_t **) PFarray_at (subexps, i))))
            return *((PFma_op_t **) PFarray_at (subexps, i));

    /* If not, add it to the list */
    *((PFma_op_t **) PFarray_add (subexps)) = n;

    return n;
}

PFma_op_t *
PFma_cse (PFma_op_t *n)
{
    subexps = PFarray (sizeof (PFma_op_t *));

    return ma_cse (n);
}
