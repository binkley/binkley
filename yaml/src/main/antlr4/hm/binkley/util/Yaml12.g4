grammar Yaml12;

/* scraped from <http://www.yaml.org/spec/1.2/spec.html> */

b_as_line_feed : b_break;

b_as_space : b_break;

b_break :
     ( b_carriage_return b_line_feed ) /* DOS, Windows */
   | b_carriage_return                 /* MacOS upto 9.x */
   | b_line_feed;                       /* UNIX, MacOS X */

b_carriage_return : '\n';    /* CR */;

b_char :
  b_line_feed | b_carriage_return;

b_chomped_last(t) :
  t = strip _> b_non_content | EOF;
   t = clip  _> b_as_line_feed | EOF;
   t = keep  _> b_as_line_feed; | EOF;

b_comment :
  b_non_content | EOF;

b_l_folded(n,c) :
  b_l_trimmed(n,c) | b_as_space;

b_l_spaced(n) :
  b_as_line_feed
   l_empty(n,block_in)*;

b_l_trimmed(n,c) :
  b_non_content l_empty(n,c)+;

b_line_feed : '\f';

b_nb_literal_next(n) :
  b_as_line_feed
   l_nb_literal_text(n);

b_non_content :
  b_break;

c_alias :
   "*";

c_anchor :
   "&";

c_b_block_header(m,t) :
   ( ( c_indentation_indicator(m)
       c_chomping_indicator(t) )
   | ( c_chomping_indicator(t)
       c_indentation_indicator(m) ) )
   s_b_comment;

c_byte_order_mark :
   '\uFEFF';

c_chomping_indicator(t) :
   "_"         _> t = strip
   "+"         _> t = keep
   /* Empty */ _> t = clip;

c_collect_entry :
   ",";

c_comment :
   "#";

c_directive :
   "%";

c_directives_end :
   "_" "_" "_";

c_document_end :
   "." "." ".";

c_double_quote :
   ["];

c_double_quoted(n,c) :
  ["] nb_double_text(n,c) ["];

c_escape :
   "\\";

c_flow_indicator :
  "," | "[" | "]" | "{" | "}";

c_flow_json_content(n,c) :
     c_flow_sequence(n,c) | c_flow_mapping(n,c)
   | c_single_quoted(n,c) | c_double_quoted(n,c);

c_flow_json_node(n,c) :
   ( c_ns_properties(n,c) s_separate(n,c) )?
   c_flow_json_content(n,c);

c_flow_mapping(n,c) :
  "{" s_separate(n,c)?
   ns_s_flow_map_entries(n,in_flow(c))? "}";

c_flow_sequence(n,c) :
  "[" s_separate(n,c)?
   ns_s_flow_seq_entries(n,in_flow(c))? "]";

c_folded :
   ">";

c_forbidden :
   /* Start of line */
   ( c_directives_end | c_document_end )
   ( b_char | s_white | EOF );

c_indentation_indicator(m) :
  ns_dec_digit _> m = ns_dec_digit _ #x30
   /* Empty */  _> m = auto_detect();

c_indicator :
     "_" | "?" | ":" | "," | "[" | "]" | "{" | "}"
   | "#" | "&" | "*" | "!" | "|" | ">" | "'" | ["]
   | "%" | "@" | "`";

c_l+folded(n) :
  ">" c_b_block_header(m,t)
   l_folded_content(n+m,t);

c_l+literal(n) :
  "|" c_b_block_header(m,t)
   l_literal_content(n+m,t);

c_l_block_map_explicit_entry(n) :
  c_l_block_map_explicit_key(n)
   ( l_block_map_explicit_value(n)
   | e_node );

c_l_block_map_explicit_key(n) :
  "?" s_l+block_indented(n,block_out);

c_l_block_map_implicit_value(n) :
  ":" ( s_l+block_node(n,block_out)
       | ( e_node s_l_comments ) );

c_l_block_seq_entry(n) :
  "_" /* Not followed by an ns_char */
   s_l+block_indented(n,block_in);

c_literal :
   "|";

c_mapping_end :
   "}";

c_mapping_key :
   "?";

c_mapping_start :
   "{";

c_mapping_value :
   ":";

c_named_tag_handle :
  "!" ns_word_char+ "!";

c_nb_comment_text :
  "#" nb_char*;

c_non_specific_tag :
  "!";

c_ns_alias_node :
  "*" ns_anchor_name;

c_ns_anchor_property :
  "&" ns_anchor_name;

c_ns_esc_char :
  "\\"
   ( ns_esc_null | ns_esc_bell | ns_esc_backspace
   | ns_esc_horizontal_tab | ns_esc_line_feed
   | ns_esc_vertical_tab | ns_esc_form_feed
   | ns_esc_carriage_return | ns_esc_escape | ns_esc_space
   | ns_esc_double_quote | ns_esc_slash | ns_esc_backslash
   | ns_esc_next_line | ns_esc_non_breaking_space
   | ns_esc_line_separator | ns_esc_paragraph_separator
   | ns_esc_8_bit | ns_esc_16_bit | ns_esc_32_bit );

c_ns_flow_map_adjacent_value(n,c) :
  ":" ( ( s_separate(n,c)?
           ns_flow_node(n,c) )
       | e_node ) /* Value */;

c_ns_flow_map_empty_key_entry(n,c) :
  e_node /* Key */
   c_ns_flow_map_separate_value(n,c);

c_ns_flow_map_json_key_entry(n,c) :
  c_flow_json_node(n,c)
   ( ( s_separate(n,c)?
       c_ns_flow_map_adjacent_value(n,c) )
   | e_node );

c_ns_flow_map_separate_value(n,c) :
  ":" /* Not followed by an
          ns_plain_safe(c) */
   ( ( s_separate(n,c) ns_flow_node(n,c) )
   | e_node /* Value */ );

c_ns_flow_pair_json_key_entry(n,c) :
  c_s_implicit_json_key(flow_key)
   c_ns_flow_map_adjacent_value(n,c);

c_ns_local_tag_prefix :
  "!" ns_uri_char*;

c_ns_properties(n,c) :
     ( c_ns_tag_property
       ( s_separate(n,c) c_ns_anchor_property )? )
   | ( c_ns_anchor_property
       ( s_separate(n,c) c_ns_tag_property )? );

c_ns_shorthand_tag :
  c_tag_handle ns_tag_char+;

c_ns_tag_property :
     c_verbatim_tag
   | c_ns_shorthand_tag
   | c_non_specific_tag;

c_primary_tag_handle :
  "!";

c_printable :
     #x9 | #xA | #xD | [#x20_#x7E]          /* 8 bit */
   | #x85 | [#xA0_#xD7FF] | [#xE000_#xFFFD] /* 16 bit */
   | [#x10000_#x10FFFF]                     /* 32 bit */;

c_quoted_quote :
  "'" "'";

c_reserved :
   "@" | "`";

c_s_implicit_json_key(c) :
  c_flow_json_node(n/a,c) s_separate_in_line?
   /* At most 1024 characters altogether */;

c_secondary_tag_handle :
  "!" "!";

c_sequence_end :
   "]";

c_sequence_entry :
   "_";

c_sequence_start :
   "[";

c_single_quote :
   "'";

c_single_quoted(n,c) :
  "'" nb_single_text(n,c) "'";

c_tag :
   "!";

c_tag_handle :
     c_named_tag_handle
   | c_secondary_tag_handle
   | c_primary_tag_handle;

c_verbatim_tag :
  "!" "<" ns_uri_char+ ">";

e_node :
  e_scalar;

e_scalar :
   /* Empty */;

in_flow(c) :
  c = flow_out  _> flow_in
   c = flow_in   _> flow_in
   c = block_key _> flow_key
   c = flow_key  _> flow_key;

l+block_mapping(n) :
   ( s_indent(n+m) ns_l_block_map_entry(n+m) )+
   /* For some fixed auto_detected m > 0 */;

l+block_sequence(n) :
   ( s_indent(n+m) c_l_block_seq_entry(n+m) )+
   /* For some fixed auto_detected m > 0 */;

l_any_document :
     l_directive_document
   | l_explicit_document
   | l_bare_document;

l_bare_document :
  s_l+block_node(_1,block_in)
   /* Excluding c_forbidden content */;

l_block_map_explicit_value(n) :
  s_indent(n)
   ":" s_l+block_indented(n,block_out);

l_chomped_empty(n,t) :
  t = strip _> l_strip_empty(n)
   t = clip  _> l_strip_empty(n)
   t = keep  _> l_keep_empty(n);

l_comment :
  s_separate_in_line c_nb_comment_text? b_comment;

l_directive :
  "%"
   ( ns_yaml_directive
   | ns_tag_directive
   | ns_reserved_directive )
   s_l_comments;

l_directive_document :
  l_directive+
   l_explicit_document;

l_document_prefix :
  c_byte_order_mark? l_comment*;

l_document_suffix :
  c_document_end s_l_comments;

l_empty(n,c) :
   ( s_line_prefix(n,c) | s_indent(<n) )
   b_as_line_feed;

l_explicit_document :
  c_directives_end
   ( l_bare_document
   | ( e_node s_l_comments ) );

l_folded_content(n,t) :
   ( l_nb_diff_lines(n) b_chomped_last(t) )?
   l_chomped_empty(n,t);

l_keep_empty(n) :
  l_empty(n,block_in)*
   l_trail_comments(n)?;

l_literal_content(n,t) :
   ( l_nb_literal_text(n) b_nb_literal_next(n)*
     b_chomped_last(t) )?
   l_chomped_empty(n,t);

l_nb_diff_lines(n) :
  l_nb_same_lines(n)
   ( b_as_line_feed l_nb_same_lines(n) )*;

l_nb_folded_lines(n) :
  s_nb_folded_text(n)
   ( b_l_folded(n,block_in) s_nb_folded_text(n) )*;

l_nb_literal_text(n) :
  l_empty(n,block_in)*
   s_indent(n) nb_char+;

l_nb_same_lines(n) :
  l_empty(n,block_in)*
   ( l_nb_folded_lines(n) | l_nb_spaced_lines(n) );

l_nb_spaced_lines(n) :
  s_nb_spaced_text(n)
   ( b_l_spaced(n) s_nb_spaced_text(n) )*;

l_strip_empty(n) :
   ( s_indent(<=n) b_non_content )*
   l_trail_comments(n)?;

l_trail_comments(n) :
  s_indent(<n) c_nb_comment_text b_comment
   l_comment*;

l_yaml_stream :
  l_document_prefix* l_any_document?
   ( l_document_suffix+ l_document_prefix* l_any_document?
   | l_document_prefix* l_explicit_document? )*;

nb_char :
  c_printable _ b_char _ c_byte_order_mark;

nb_double_char :
  c_ns_esc_char | ( nb_json _ "\\" _ ["] );

nb_double_multi_line(n) :
  nb_ns_double_in_line
   ( s_double_next_line(n) | s_white* );

nb_double_one_line :
  nb_double_char*;

nb_double_text(n,c) :
  c = flow_out  _> nb_double_multi_line(n)
   c = flow_in   _> nb_double_multi_line(n)
   c = block_key _> nb_double_one_line
   c = flow_key  _> nb_double_one_line;

nb_json :
   #x9 | [#x20_#x10FFFF];

nb_ns_double_in_line :
   ( s_white* ns_double_char )*;

nb_ns_plain_in_line(c) :
   ( s_white* ns_plain_char(c) )*;

nb_ns_single_in_line :
   ( s_white* ns_single_char )*;

nb_single_char :
  c_quoted_quote | ( nb_json _ "'" );

nb_single_multi_line(n) :
  nb_ns_single_in_line
   ( s_single_next_line(n) | s_white* );

nb_single_one_line :
  nb_single_char*;

nb_single_text(n,c) :
  c = flow_out  _> nb_single_multi_line(n)
   c = flow_in   _> nb_single_multi_line(n)
   c = block_key _> nb_single_one_line
   c = flow_key  _> nb_single_one_line;

ns_anchor_char :
  ns_char _ c_flow_indicator;

ns_anchor_name :
  ns_anchor_char+;

ns_ascii_letter :
   [#x41_#x5A] /* A_Z */ | [#x61_#x7A] /* a_z */;

ns_char :
  nb_char _ s_white;

ns_dec_digit :
   [#x30_#x39] /* 0_9 */;

ns_directive_name :
  ns_char+;

ns_directive_parameter :
  ns_char+;

ns_double_char :
  nb_double_char _ s_white;

ns_esc_16_bit :
   "u"
   ( ns_hex_digit{4} );

ns_esc_32_bit :
   "U"
   ( ns_hex_digit{8} );

ns_esc_8_bit :
   "x"
   ( ns_hex_digit{2} );

ns_esc_backslash :
  "\\";

ns_esc_backspace :
   "b";

ns_esc_bell :
   "a";

ns_esc_carriage_return :
   "r";

ns_esc_double_quote :
  ["];

ns_esc_escape :
   "e";

ns_esc_form_feed :
   "f";

ns_esc_horizontal_tab :
   "t" | #x9;

ns_esc_line_feed :
   "n";

ns_esc_line_separator :
   "L";

ns_esc_next_line :
   "N";

ns_esc_non_breaking_space :
   "_";

ns_esc_null :
   "0";

ns_esc_paragraph_separator :
   "P";

ns_esc_slash :
   "/";

ns_esc_space :
   #x20;

ns_esc_vertical_tab :
   "v";

ns_flow_content(n,c) :
  ns_flow_yaml_content(n,c) | c_flow_json_content(n,c);

ns_flow_map_entry(n,c) :
     ( "?" s_separate(n,c)
       ns_flow_map_explicit_entry(n,c) )
   | ns_flow_map_implicit_entry(n,c);

ns_flow_map_explicit_entry(n,c) :
     ns_flow_map_implicit_entry(n,c)
   | ( e_node /* Key */
       e_node /* Value */ );

ns_flow_map_implicit_entry(n,c) :
     ns_flow_map_yaml_key_entry(n,c)
   | c_ns_flow_map_empty_key_entry(n,c)
   | c_ns_flow_map_json_key_entry(n,c);

ns_flow_map_yaml_key_entry(n,c) :
  ns_flow_yaml_node(n,c)
   ( ( s_separate(n,c)?
       c_ns_flow_map_separate_value(n,c) )
   | e_node );

ns_flow_node(n,c) :
     c_ns_alias_node
   | ns_flow_content(n,c)
   | ( c_ns_properties(n,c)
       ( ( s_separate(n,c)
           ns_flow_content(n,c) )
         | e_scalar ) );

ns_flow_pair(n,c) :
     ( "?" s_separate(n,c)
       ns_flow_map_explicit_entry(n,c) )
   | ns_flow_pair_entry(n,c);

ns_flow_pair_entry(n,c) :
     ns_flow_pair_yaml_key_entry(n,c)
   | c_ns_flow_map_empty_key_entry(n,c)
   | c_ns_flow_pair_json_key_entry(n,c);

ns_flow_pair_yaml_key_entry(n,c) :
  ns_s_implicit_yaml_key(flow_key)
   c_ns_flow_map_separate_value(n,c);

ns_flow_seq_entry(n,c) :
  ns_flow_pair(n,c) | ns_flow_node(n,c);

ns_flow_yaml_content(n,c) :
  ns_plain(n,c);

ns_flow_yaml_node(n,c) :
     c_ns_alias_node
   | ns_flow_yaml_content(n,c)
   | ( c_ns_properties(n,c)
       ( ( s_separate(n,c)
           ns_flow_yaml_content(n,c) )
         | e_scalar ) );

ns_global_tag_prefix :
  ns_tag_char ns_uri_char*;

ns_hex_digit :
     ns_dec_digit
   | [#x41_#x46] /* A_F */ | [#x61_#x66] /* a_f */;

ns_l_block_map_entry(n) :
     c_l_block_map_explicit_entry(n)
   | ns_l_block_map_implicit_entry(n);

ns_l_block_map_implicit_entry(n) :
   ( ns_s_block_map_implicit_key
   | e_node )
   c_l_block_map_implicit_value(n);

ns_l_compact_mapping(n) :
  ns_l_block_map_entry(n)
   ( s_indent(n) ns_l_block_map_entry(n) )*;

ns_l_compact_sequence(n) :
  c_l_block_seq_entry(n)
   ( s_indent(n) c_l_block_seq_entry(n) )*;

ns_plain(n,c) :
  c = flow_out  _> ns_plain_multi_line(n,c)
   c = flow_in   _> ns_plain_multi_line(n,c)
   c = block_key _> ns_plain_one_line(c)
   c = flow_key  _> ns_plain_one_line(c);

ns_plain_char(c) :
     ( ns_plain_safe(c) _ ":" _ "#" )
   | ( /* An ns_char preceding */ "#" )
   | ( ":" /* Followed by an ns_plain_safe(c) */ );

ns_plain_first(c) :
     ( ns_char _ c_indicator )
   | ( ( "?" | ":" | "_" )
       /* Followed by an ns_plain_safe(c)) */ );

ns_plain_multi_line(n,c) :
  ns_plain_one_line(c)
   s_ns_plain_next_line(n,c)*;

ns_plain_one_line(c) :
  ns_plain_first(c) nb_ns_plain_in_line(c);

ns_plain_safe(c) :
  c = flow_out  _> ns_plain_safe_out
   c = flow_in   _> ns_plain_safe_in
   c = block_key _> ns_plain_safe_out
   c = flow_key  _> ns_plain_safe_in;

ns_plain_safe_in :
  ns_char _ c_flow_indicator;

ns_plain_safe_out :
  ns_char;

ns_reserved_directive :
  ns_directive_name
   ( s_separate_in_line ns_directive_parameter )*;

ns_s_block_map_implicit_key :
     c_s_implicit_json_key(block_key)
   | ns_s_implicit_yaml_key(block_key);

ns_s_flow_map_entries(n,c) :
  ns_flow_map_entry(n,c) s_separate(n,c)?
   ( "," s_separate(n,c)?
     ns_s_flow_map_entries(n,c)? )?;

ns_s_flow_seq_entries(n,c) :
  ns_flow_seq_entry(n,c) s_separate(n,c)?
   ( "," s_separate(n,c)?
     ns_s_flow_seq_entries(n,c)? )?;

ns_s_implicit_yaml_key(c) :
  ns_flow_yaml_node(n/a,c) s_separate_in_line?
   /* At most 1024 characters altogether */;

ns_single_char :
  nb_single_char _ s_white;

ns_tag_char :
  ns_uri_char _ "!" _ c_flow_indicator;

ns_tag_directive :
   "T" "A" "G"
   s_separate_in_line c_tag_handle
   s_separate_in_line ns_tag_prefix;

ns_tag_prefix :
  c_ns_local_tag_prefix | ns_global_tag_prefix;

ns_uri_char :
     "%" ns_hex_digit ns_hex_digit | ns_word_char | "#"
   | ";" | "/" | "?" | ":" | "@" | "&" | "=" | "+" | "$" | ","
   | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")" | "[" | "]";

ns_word_char :
  ns_dec_digit | ns_ascii_letter | "_";

ns_yaml_directive :
   "Y" "A" "M" "L"
   s_separate_in_line ns_yaml_version;

ns_yaml_version :
  ns_dec_digit+ "." ns_dec_digit+;

s_b_comment :
   ( s_separate_in_line c_nb_comment_text? )?
   b_comment;

s_block_line_prefix(n) :
  s_indent(n);

s_double_break(n) :
  s_double_escaped(n) | s_flow_folded(n);

s_double_escaped(n) :
  s_white* "\\" b_non_content
   l_empty(n,flow_in)* s_flow_line_prefix(n);

s_double_next_line(n) :
  s_double_break(n)
   ( ns_double_char nb_ns_double_in_line
     ( s_double_next_line(n) | s_white* ) )?;

s_flow_folded(n) :
  s_separate_in_line? b_l_folded(n,flow_in)
   s_flow_line_prefix(n);

s_flow_line_prefix(n) :
  s_indent(n) s_separate_in_line?;

s_indent(<n) :
  s_space{m} /* Where m < n */;

s_indent(n) :
  s_space{n};

s_indent(<=n) :
  s_space{m} /* Where m <= n */;

s_l+block_collection(n,c) :
   ( s_separate(n+1,c) c_ns_properties(n+1,c) )?
   s_l_comments
   ( l+block_sequence(seq_spaces(n,c))
   | l+block_mapping(n) );

s_l+block_in_block(n,c) :
  s_l+block_scalar(n,c) | s_l+block_collection(n,c);

s_l+block_indented(n,c) :
     ( s_indent(m)
       ( ns_l_compact_sequence(n+1+m)
       | ns_l_compact_mapping(n+1+m) ) )
   | s_l+block_node(n,c)
   | ( e_node s_l_comments );

s_l+block_node(n,c) :
  s_l+block_in_block(n,c) | s_l+flow_in_block(n);

s_l+block_scalar(n,c) :
  s_separate(n+1,c)
   ( c_ns_properties(n+1,c) s_separate(n+1,c) )?
   ( c_l+literal(n) | c_l+folded(n) );

s_l+flow_in_block(n) :
  s_separate(n+1,flow_out)
   ns_flow_node(n+1,flow_out) s_l_comments;

s_l_comments :
   ( s_b_comment | /* Start of line */ )
   l_comment*;

s_line_prefix(n,c) :
  c = block_out _> s_block_line_prefix(n)
   c = block_in  _> s_block_line_prefix(n)
   c = flow_out  _> s_flow_line_prefix(n)
   c = flow_in   _> s_flow_line_prefix(n);

s_nb_folded_text(n) :
  s_indent(n) ns_char nb_char*;

s_nb_spaced_text(n) :
  s_indent(n) s_white nb_char*;

s_ns_plain_next_line(n,c) :
  s_flow_folded(n)
   ns_plain_char(c) nb_ns_plain_in_line(c);

s_separate(n,c) :
  c = block_out _> s_separate_lines(n)
   c = block_in  _> s_separate_lines(n)
   c = flow_out  _> s_separate_lines(n)
   c = flow_in   _> s_separate_lines(n)
   c = block_key _> s_separate_in_line
   c = flow_key  _> s_separate_in_line;

s_separate_in_line :
  s_white+ | /* Start of line */;

s_separate_lines(n) :
     ( s_l_comments s_flow_line_prefix(n) )
   | s_separate_in_line;

s_single_next_line(n) :
  s_flow_folded(n)
   ( ns_single_char nb_ns_single_in_line
     ( s_single_next_line(n) | s_white* ) )?;

s_space :
   #x20 /* SP */;

s_tab :
   #x9  /* TAB */;

s_white :
  s_space | s_tab;

seq_spaces(n,c) :
  c = block_out _> n_1
   c = block_in  _> n
