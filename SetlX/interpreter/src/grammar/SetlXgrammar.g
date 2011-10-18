grammar SetlXgrammar;

@header {
    package grammar;

    import interpreter.boolExpressions.*;
    import interpreter.expressions.*;
    import interpreter.statements.*;
    import interpreter.types.*;
    import interpreter.utilities.*;

    import java.util.LinkedList;
    import java.util.List;
}

@lexer::header {
    package grammar;
}

block returns [Block blk]
    @init{
        List<Statement>      stmnts     = new LinkedList<Statement>();
    }
    :
       (
         statement      { stmnts.add($statement.stmnt);   }
       )*
       { blk = new Block(stmnts);        }
    ;

statement returns [Statement stmnt]
    @init{
        List<BranchAbstract> branchList = new LinkedList<BranchAbstract>();
    }
    :
      'var' ID ';'                                            { stmnt = new GlobalDefinition($ID.text);             }
    | expr ';'                                                { stmnt = new ExpressionStatement($expr.ex);          }
    | 'if'          '(' c1 = condition ')' '{' b1 = block '}' { branchList.add(new BranchIf($c1.bex, $b1.blk));     }
      (
        'else' 'if' '(' c2 = condition ')' '{' b2 = block '}' { branchList.add(new BranchElseIf($c2.bex, $b2.blk)); }
      )*
      (
        'else'                             '{' b3 = block '}' { branchList.add(new BranchElse($b3.blk));            }
      )?
      { stmnt = new IfThen(branchList); }
    | 'switch' '{'
      (
        'case' c1 = condition ':' b1 = block                  { branchList.add(new BranchCase($c1.bex, $b1.blk));   }
      )*
      (
        'default'             ':' b2 = block                  { branchList.add(new BranchDefault($b2.blk));         }
      )?
      '}' { stmnt = new Switch(branchList); }
    | 'for'   '(' iterator  ')' '{' block '}'                 { stmnt = new For($iterator.iter, $block.blk);        }
    | 'while' '(' condition ')' '{' block '}'                 { stmnt = new While($condition.bex, $block.blk);      }
    | 'return' expr? ';'                                      { stmnt = new Return($expr.ex);                       }
    | 'continue' ';'                                          { stmnt = new Continue();                             }
    | 'break' ';'                                             { stmnt = new Break();                                }
    | 'exit' ';'                                              { stmnt = new Exit();                                 }
    ;

condition returns [BoolExpr bex]
    :
      expr   { bex = new BoolExpr($expr.ex); }
    ;

expr returns [Expr ex]
    :
      (assignment)=>assignment { ex = $assignment.assign;                                    }
    | 'forall' '(' iterator '|' condition ')'
                               { ex = new Forall($iterator.iter, $condition.bex);            }
    | 'exists' '(' iterator '|' condition ')'
                               { ex = new Exists($iterator.iter, $condition.bex);            }
    | c1 = conjunction         {ex = $c1.c;                                                  }
      (
        '||' c2 = conjunction  {ex = new Disjunction(new BoolExpr(ex), new BoolExpr($c2.c)); }
      )*
    ;

assignment returns [Assignment assign]
    @init {
        AssignmentLhs lhs   = null;
        List<Expr>    items = new ArrayList<Expr>();
        int           type  = -1;
    }
    :
      (
         ID
         (
           '(' sum ')'  { items.add($sum.s);                        }
         )*             { lhs = new AssignmentLhs($ID.text, items); }
       | list           { lhs = new AssignmentLhs($list.lc);        }
      )
      (
         ':='           { type = Assignment.DIRECT; }
       | '+='           { type = Assignment.SUM; }
       | '-='           { type = Assignment.DIFFERENCE; }
       | '*='           { type = Assignment.PRODUCT; }
       | '/='           { type = Assignment.DIVISION; }
      )
      expr              { $assign = new Assignment(lhs, type, $expr.ex); }
    ;

conjunction returns [Expr c]
    :
      e1 = equation        {c = $e1.eq;                                                 }
      (
        '&&' e2 = equation {c = new Conjunction(new BoolExpr(c), new BoolExpr($e2.eq)); }
      )*
    ;

equation returns [Expr eq]
    @init{
        int type = -1;
    }
    :
      c1 = comparison   { eq = $c1.comp;                                }
      (
        (
           '=='         { type = Comparison.EQUAL;                      }
         | '!='         { type = Comparison.UNEQUAL;                    }
        )
        c2 = comparison { eq = new Comparison (eq, type, $c2.comp);     }
      )*
    ;

comparison returns [Expr comp]
    @init{
        int type = -1;
    }
    :
      i1 = inclusion    { comp = $i1.incl;                              }
      (
        (
           '<'          { type = Comparison.LESSTHAN;                   }
         | '<='         { type = Comparison.EQUALORLESS;                }
         | '>'          { type = Comparison.MORETHAN;                   }
         | '>='         { type = Comparison.EQUALORMORE;                }
        )
        i2 = inclusion  { comp = new Comparison (comp, type, $i2.incl); }
      )*
    ;

inclusion returns [Expr incl]
    @init{
        int type = -1;
    }
    :
      s1 = sum          { incl = $s1.s;                                 }
      (
        (
            'in'        { type = Comparison.IN;                         }
          | 'notin'     { type = Comparison.NOTIN;                      }
        )
        s2 = sum        { incl = new Comparison (incl, type, $s2.s);    }
      )*
    ;

sum returns [Expr s]
    :
      p1 = product             { s = $p1.p;                         }
      (
          '+'  p2 = product    { s = new Sum(s, $p2.p);             }
        | '-'  p2 = product    { s = new Difference(s, $p2.p);      }
        | '+/' p2 = product    { s = new SumMembers(s, $p2.p);      }
      )*
    ;

product returns [Expr p]
    :
      pow1 = power             { p = $pow1.pow;                         }
      (
          '*'   pow2 = power   { p = new Product(p, $pow2.pow);         }
        | '/'   pow2 = power   { p = new Division(p, $pow2.pow);        }
        | '*/'  pow2 = power   { p = new MultiplyMembers(p, $pow2.pow); }
        | '%'   pow2 = power   { p = new Modulo(p, $pow2.pow);          }
      )*
    ;

power returns [Expr pow]
    :
      minmax           { pow = $minmax.mm;              }
      (
        '**' p = power { pow = new Power (pow, $p.pow); }
      )?
    ;

minmax returns [Expr mm]
    :
      factor                { mm = $factor.f;                    }
      (
          'min'  m = minmax { mm = new Minimum(mm, $m.mm);       }
        | 'min/' m = minmax { mm = new MinimumMember(mm, $m.mm); }
        | 'max'  m = minmax { mm = new Maximum(mm, $m.mm);       }
        | 'max/' m = minmax { mm = new MaximumMember(mm, $m.mm); }
      )?
    ;

factor returns [Expr f]
    :
      '(' expr ')'             { f = new BracketedExpr($expr.ex);       }
    | 'arb'        fa = factor { f = new Arb($fa.f);                    }
    | 'from'       fa = factor { f = new From($fa.f);                   }
    | 'fromb'      fa = factor { f = new FromB($fa.f);                  }
    | 'frome'      fa = factor { f = new FromE($fa.f);                  }
    | 'pow'        fa = factor { f = new Pow($fa.f);                    }
    | 'min/'       fa = factor { f = new MinimumMember(null, $fa.f);    }
    | 'max/'       fa = factor { f = new MaximumMember(null, $fa.f);    }
    | '+/'         fa = factor { f = new SumMembers(null, $fa.f);       }
    | '*/'         fa = factor { f = new MultiplyMembers(null, $fa.f);  }
    | '-'          fa = factor { f = new Negative($fa.f);               }
    | '!'          fa = factor { f = new Negation(new BoolExpr($fa.f)); }
    | '#'          fa = factor { f = new Cardinality($fa.f);            }
    | 'abs'        fa = factor { f = new Abs($fa.f);                    }
    | 'char'       fa = factor { f = new Char($fa.f);                   }
    | 'domain'     fa = factor { f = new Domain($fa.f);                 }
    | 'is_integer' fa = factor { f = new IsInteger($fa.f);              }
    | 'is_map'     fa = factor { f = new IsMap($fa.f);                  }
    | 'is_real'    fa = factor { f = new IsReal($fa.f);                 }
    | 'is_set'     fa = factor { f = new IsSet($fa.f);                  }
    | 'is_string'  fa = factor { f = new IsString($fa.f);               }
    | 'is_tuple'   fa = factor { f = new IsTuple($fa.f);                }
    | 'range'      fa = factor { f = new RelationalRange($fa.f);        }
    | 'str'        fa = factor { f = new Str($fa.f);                    }
    | call                     { f = $call.c;                           }
    | definition               { f = new ValueExpr($definition.dfntn);  }
    | list                     { f = $list.lc;                          }
    | set                      { f = $set.sc;                           }
    | value                    { f = new ValueExpr($value.v);           }
    ;

// this could be either 'id' or 'call' or 'element of collection'
// decide at runtime
call returns [ Expr c ]
    @init {
        List<Expr> args = null;
        boolean relation = false;
    }
    :
      ID          { c = new Variable($ID.text); }
      (
        (
            '('   { relation = false; }
          | '{'   { relation = true;  }
        )         { args = new ArrayList<Expr>(); }
        (
            e1 = expr           { args.add($e1.ex);                                     }
            (
                (
                  ',' e2 = expr { args.add($e2.ex);                                     }
                )*
              | '..'            { args.add(CallRangeDummy.CRD);                         }
                (
                  s1 = sum      { args.add($s1.s);                                      }
                )?
            )
          | '..' s1 = sum       { args.add(CallRangeDummy.CRD); args.add($s1.s);        }
        )?
        (
            ')'  { if( relation) System.err.println("Closing bracket does not match!"); }
          | '}'  { if(!relation) System.err.println("Closing bracket does not match!"); }
        )        { c = new Call(c, args, relation); }
      )*
    ;

definition returns [SetlDefinition dfntn]
    @init{
        List<Statement>      stmnts = new LinkedList<Statement>();
        List<SetlDefinition> dfntns = new LinkedList<SetlDefinition>();
    }
    :
      'procedure' n1 = ID '(' p = paramDefinitionList ')' ';'
        (
            s = statement  { stmnts.add($s.stmnt); }
        )*
      'end' n2 = ID ';'
      {
        if(!($n1.text).equals($n2.text)){
            System.err.println("Procedure name `"+ $n1.text +"´ does not match procedure end `"+ $n2.text +"´!");
        }
        dfntn = new SetlDefinition($n1.text, $p.paramList, stmnts, dfntns);
      }
    ;

paramDefinitionList returns [List<String> paramList]
    @init {
        List<String> list = new ArrayList<String>();
    }
    :
      (
        i1 = ID { list.add($i1.text); }
        (
           ',' i2 = ID { list.add($i2.text); }
        )*
      )?
      { paramList = list; }
    ;

value returns [Value v]
    :
      NUMBER        { v = new SetlInt($NUMBER.text);      }
    | real          { v = $real.r;                        }
    | STRING        { v = new SetlString($STRING.text);   }
    | 'true'        { v = SetlBoolean.TRUE;               }
    | 'false'       { v = SetlBoolean.FALSE;              }
    | 'om'          { v = SetlOm.OM;                      }
    ;

real returns [SetlReal r]
    @init {
        String n = "";
    }
    :
      (
        NUMBER                  { n = $NUMBER.text;                  }
      )? REAL                   { r = new SetlReal(n + $REAL.text);  }
    ;

list returns [SetListConstructor lc]
    :
      '[' constructor? ']' { lc = new SetListConstructor(SetListConstructor.LIST, $constructor.c); }
    ;

set returns [SetListConstructor sc]
    :
      '{' constructor? '}' { sc = new SetListConstructor(SetListConstructor.SET, $constructor.c); }
    ;

constructor returns [Constructor c]
    :
      ( range   )=> range   { c = $range.r;         }
    | ( iterate )=> iterate { c = $iterate.i;       }
    | explicitList          { c = $explicitList.el; }
    ;

range returns [Range r]
    @init {
        Expr e = null;
    }
    :
      e1 = expr
      (
        ',' e2 = expr { e = $e2.ex; }
      )?
      '..' e3 = expr
      { r = new Range($e1.ex, e, $e3.ex); }
    ;

iterate returns [Iteration i]
    @init {
        BoolExpr bex = null;
    }
    :
        (shortIterate)=>shortIterate {i = $shortIterate.si; }
      | e1 = expr ':' iterator
        (
          '|' b = expr  { bex = new BoolExpr($b.ex);                     }
        )?
        { i = new Iteration($e1.ex, $iterator.iter, bex); }
    ;

iterator returns [Iterator iter]
    @init{
        String               id         = null;
        SetListConstructor   lc         = null;
    }
    : ( i1 = ID | l1 = list )
      'in' e1 = expr         { iter = new Iterator($i1.text, $l1.lc, $e1.ex); }
      (
        ','
        (
            i2 = ID          { id = $i2.text; lc = null;   }
          | l2 = list        { id = null;     lc = $l2.lc; }
        )
        'in' e2 = expr       { iter.add(new Iterator(id, lc, $e2.ex)); }
      )*
    ;

shortIterate returns [Iteration si]
    @init {
        BoolExpr bex = null;
    }
    :
      (
          ID
        | list
      )
      'in' e1 = expr
      (
        '|' b = expr { bex = new BoolExpr($b.ex); }
      )?
      { si = new Iteration(null, new Iterator($ID.text, $list.lc, $e1.ex) , bex); }
    ;

explicitList returns [ExplicitList el]
    @init {
        List<Expr> exprs = new ArrayList<Expr>();
    }
    :
      e1 = expr        { exprs.add($e1.ex); }
      (
        ',' e2 = expr  { exprs.add($e2.ex); }
      )*
      { el = new ExplicitList(exprs); }
    ;

ID              : ('a' .. 'z' | 'A' .. 'Z')('a' .. 'z' | 'A' .. 'Z'|'_'|'0' .. '9')* ;
NUMBER          : '0'|('1' .. '9')('0' .. '9')*;
REAL            : '.'('0' .. '9')+ (('e'|'E') '-'? ('0' .. '9')+)? ;
STRING          : '"' ('\\"'|~('"'))* '"';

LINE_COMMENT    : '//' ~('\n')*                             { skip(); } ;
MULTI_COMMENT   : '/*' (~('*') | '*'+ ~('*'|'/'))* '*'+ '/' { skip(); } ;
WS              : (' '|'\t'|'\n'|'r')                       { skip(); } ;
