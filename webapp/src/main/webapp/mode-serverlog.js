ace.define("ace/mode/server_log_highlight_rules",["require","exports","module","ace/lib/oop","ace/mode/text_highlight_rules"], function(require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var ServerLogHighlightRules = function() {

        this.$rules = {
            "start" : [
                {
                    token: "warn",
                    regex: /^[\d\-\s,:]+WARN.*$/
                },
                {
                    token: "error",
                    regex: /^[\d\-\s,:]+ERROR.*$/
                },
                {
                    token: "started_in",
                    regex: /^.*started in \d+.*$/
                },
            ]
        };

    };

    oop.inherits(ServerLogHighlightRules, TextHighlightRules);

    exports.ServerLogHighlightRules = ServerLogHighlightRules;
});

ace.define("ace/mode/serverlog",["require","exports","module","ace/lib/oop","ace/mode/text","ace/mode/server_log_highlight_rules"], function(require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var ServerLogHighlightRules = require("./server_log_highlight_rules").ServerLogHighlightRules;

    var Mode = function() {
        this.HighlightRules = ServerLogHighlightRules;
        this.$behaviour = this.$defaultBehaviour;
    };
    oop.inherits(Mode, TextMode);

    (function() {
        this.$id = "ace/mode/serverlog";
    }).call(Mode.prototype);

    exports.Mode = Mode;
});

(function() {
    ace.require(["ace/mode/serverlog"], function(m) {
        if (typeof module == "object" && typeof exports == "object" && module) {
            module.exports = m;
        }
    });
})();
            