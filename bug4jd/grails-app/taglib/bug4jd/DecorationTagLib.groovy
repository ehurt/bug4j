/*
 * Copyright 2012 Cedric Dandoy
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package bug4jd

class DecorationTagLib {
    Closure expansionToggle = { attrs, body ->
        final section = attrs.remove('section')
        boolean expanded = isExpanded(section)

        String visibleStyle = "cursor: pointer;"
        String hiddenStyle = visibleStyle + "display: none;"

        out << '<div style="font-size:1.5em;font-weight: bold;">'
        out << img(
                class: "collapse-section-img",
                dir: "images/skin",
                file: "collapse_16.png",
                style: expanded ? visibleStyle : hiddenStyle,
                onclick: "collapseSection('${section}');",
        )
        out << img(
                class: "expand-section-img",
                dir: "images/skin",
                file: "expand_16.png",
                style: !expanded ? visibleStyle : hiddenStyle,
                onclick: "expandSection('${section}');",
        )

        out << "&nbsp;"

        out << body()
        out << '</div>'
    }

    Closure expansionSection = { attrs, body ->
        final section = attrs.remove('section')
        if (!section) {
            throw new IllegalArgumentException('Missing section')
        }
        boolean expanded = isExpanded(section)

        boolean hadStyle
        String style
        if (expanded) {
            hadStyle = true
            style = ''
        } else {
            hadStyle = false
            style = 'display:none;'
        }

        out << '<div '
        attrs.each { k, v ->
            if ('style' == k) {
                v += ';' + style
                hadStyle = true
            }
            out << "${k}=\"${v?.encodeAsHTML()}\" "
        }

        if (!hadStyle) {
            out << "style='${style}'"
        }

        out << '>'

        out << body()
        out << '</div>'
    }

    private def isExpanded = {section ->
        boolean expanded = true
        final expandPreferences = pageScope.expandPreferences
        if (expandPreferences) {
            final expandedValue = expandPreferences['expand.' + section]
            expanded = !"collapsed".equals(expandedValue)
        }
        return expanded
    }
}
